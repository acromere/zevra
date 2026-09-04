package com.acromere.event;

import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

public class EventWatcher<T extends Event> implements EventHandler<T> {

	public static final long DEFAULT_WAIT_TIMEOUT = 5000;

	private final Queue<T> events = new ConcurrentLinkedQueue<>();

	@Getter
	private final long timeout;

	@Getter
	@Setter
	private boolean printEventCapture;

	public EventWatcher() {
		this( DEFAULT_WAIT_TIMEOUT );
	}

	public EventWatcher( long timeout ) {
		this.timeout = timeout;
	}

	@Override
	@SuppressWarnings( "java:S106" )
	public synchronized void handle( T event ) {
		if( printEventCapture ) System.out.println( "Captured event: type=" + event.getEventType() );
		events.offer( event );
		notifyAll();
	}

	public void waitForEvent( EventType<T> type ) throws InterruptedException, TimeoutException {
		waitForEvent( type, timeout );
	}

	@SuppressWarnings( "unused" )
	public void waitForNextEvent( EventType<T> type ) throws InterruptedException, TimeoutException {
		waitForNextEvent( type, timeout );
	}

	/**
	 * Wait for an event of a specific class to occur. If the event has already
	 * occurred, this method will return immediately. If the event has not
	 * already occurred, then this method waits until the next event occurs, or
	 * the specified timeout, whichever comes first.
	 *
	 * @param type The event type to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForEvent( EventType<T> type, long timeout ) throws InterruptedException, TimeoutException {
		boolean shouldWait = timeout > 0;
		long start = System.currentTimeMillis();
		long expiration = start + timeout;
		long duration = expiration - System.currentTimeMillis();

		Event event;
		while( (event = findNext( type )) == null & shouldWait ) {
			wait( Math.max( 1, duration ) );
			duration = expiration - System.currentTimeMillis();
			shouldWait = duration > 0;
		}

		if( event == null ) throw new TimeoutException( "Timeout waiting for event " + type.getParentEventType() + "." + type );
	}

	/**
	 * Wait for the next event of a specific class to occur. This method always
	 * waits until the next event occurs, or the specified timeout, whichever
	 * comes first.
	 *
	 * @param type The event class to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForNextEvent( EventType<T> type, long timeout ) throws InterruptedException, TimeoutException {
		findNext( type );
		waitForEvent( type, timeout );
	}

	private Event findNext( EventType<T> type ) {
		Event event;
		while( (event = events.poll()) != null ) {
			if( event.getEventType() == type ) return event;
		}
		return null;
	}

}
