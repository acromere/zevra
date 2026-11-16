package com.acromere.util;

import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class generalizes the concept of delayed actions that are performed
 * after some amount of "idle" time has occurred. This class also supports
 * minimum and maximum action requirements.
 */
public class DelayedAction {

	private static final long DEFAULT_MIN_TRIGGER_LIMIT = 100;

	private static final long DEFAULT_MAX_TRIGGER_LIMIT = 500;

	private static final Timer timer = new Timer( DelayedAction.class.getSimpleName() + "-Timer", true );

	private final AtomicLong lastChangeTime = new AtomicLong();

	private final AtomicLong lastActionTime = new AtomicLong();

	private final AtomicLong lastDirtyTime = new AtomicLong();

	private final Object scheduleLock = new Object();

	@Getter
	private final ExecutorService executor;

	@Getter
	private final Runnable action;

	/**
	 * Action triggered no more than this limit, regardless of how often
	 * {@link #schedule} is called.
	 */
	@Setter
	@Getter
	private long minTriggerLimit = DEFAULT_MIN_TRIGGER_LIMIT;

	/**
	 * Action will be triggered at least this often, regardless of how little
	 * {@link #schedule} is called.
	 */
	@Setter
	@Getter
	private long maxTriggerLimit = DEFAULT_MAX_TRIGGER_LIMIT;

	private ActionTask task;

	/**
	 * Creates a new delayed action without an executor or action.
	 */
	public DelayedAction() {
		this( null, null );
	}

	/**
	 * Creates a new delayed action that runs the action on the given
	 * executor, without a default action.
	 *
	 * @param executor The executor on which the action is run
	 */
	public DelayedAction( ExecutorService executor ) {
		this( executor, null );
	}

	/**
	 * Creates a new delayed action that runs the given action on the timer
	 * thread.
	 *
	 * @param action The action to run after the delay
	 */
	public DelayedAction( Runnable action ) {
		this( null, action );
	}

	/**
	 * Creates a new delayed action that runs the given action on the given
	 * executor if it is available, otherwise on the timer thread.
	 *
	 * @param executor The executor on which the action is run, or
	 *                 {@code null} to run it on the timer thread
	 * @param action The action to run after the delay
	 */
	public DelayedAction( ExecutorService executor, Runnable action ) {
		this.executor = executor;
		this.action = action;
	}

	/**
	 * Marks the action as needing to run because of a state change and
	 * reschedules it. The action will not be triggered until the
	 * {@link #minTriggerLimit} idle time has elapsed since the change.
	 */
	public void request() {
		lastChangeTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastActionTime.get() ) lastDirtyTime.set( lastChangeTime.get() );
		schedule();
	}

	/**
	 * Schedules the action to run after the trigger limits have been
	 * satisfied, if it has not already been scheduled to do so.
	 */
	public void schedule() {
		doSchedule( false );
	}

	/**
	 * Schedules the action to run as soon as possible, regardless of the
	 * trigger limits.
	 */
	public void trigger() {
		doSchedule( true );
	}

	/**
	 * Updates the last action time to now, which prevents the action from
	 * being triggered until a change is requested.
	 */
	public void reset() {
		lastActionTime.set( System.currentTimeMillis() );
	}

	/**
	 * Cancels any pending scheduled action.
	 */
	public void cancel() {
		synchronized( scheduleLock ) {
			if( task != null ) task.cancel();
		}
	}

	private void doSchedule( boolean immediate ) {
		synchronized( scheduleLock ) {
			long now = System.currentTimeMillis();
			long actionTime = lastActionTime.get();
			long changeTime = lastChangeTime.get();
			long dirtyTime = lastDirtyTime.get();
			long minNext = changeTime + minTriggerLimit;
			long maxNext = Math.max( dirtyTime, actionTime ) + maxTriggerLimit;

			boolean changesSinceLastAction = dirtyTime > actionTime;

			//System.out.println( "da=" +(dirtyTime - actionTime) + " ua=" + (updateTime - actionTime));

			// If there are no changes since the last action time, just return
			if( !immediate && !changesSinceLastAction ) return;

			long nextTime = Math.min( minNext, maxNext );
			long taskTime = task == null ? 0 : task.scheduledExecutionTime();

			//System.out.println( "task=" + task + " taskTime=" + (taskTime - actionTime) + " nextTime=" + (nextTime - actionTime) );

			// If the existing task time is already set to the next time, just return
			if( !immediate && (taskTime == nextTime) ) return;

			// Cancel the existing task and schedule a new one
			if( task != null ) task.cancel();

			try {
				task = new ActionTask();
				long delay = Math.max( 0, immediate ? 0 : nextTime - now );
				timer.schedule( task, delay );
			} catch( IllegalStateException ignore ) {
				// Intentionally ignore exception
			}
		}
	}

	private void fire() {
		reset();
		if( action != null ) action.run();
	}

	private class ActionTask extends TimerTask {

		@Override
		public void run() {
			// If there is an executor, use it to run the task, otherwise run the task on the timer thread
			if( executor != null && !executor.isShutdown() ) {
				executor.submit( DelayedAction.this::fire );
			} else {
				fire();
			}
		}

	}

}
