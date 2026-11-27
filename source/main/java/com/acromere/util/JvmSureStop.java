package com.acromere.util;

import lombok.CustomLog;

import java.util.Arrays;
import java.util.Map;

/**
 * This shutdown hook is used to ensure that the program eventually terminates
 * given a specific amount of time. There are times when misbehaving threads
 * cause the program not to exit cleanly. This hook waits for the program to
 * terminate for a specific amount of time. Once that time expires, this hook
 * logs the list of running non-daemon threads and forces the program to stop
 * by calling Runtime.halt(). Unfortunately, this also causes other shutdown
 * hooks to not execute if they have not started.
 *
 * @author soderquistmv
 */
@CustomLog
public class JvmSureStop extends Thread {

	/**
	 * The amount of time to give the JVM to exit cleanly. After this amount of
	 * time the JVM is halted by calling Runtime.getRuntime().halt().
	 */
	public static final int DEFAULT_WAIT_DELAY = 15000;

	private final int delay;

	/**
	 * Creates a new shutdown hook that waits the default amount of time for
	 * the JVM to exit cleanly.
	 */
	public JvmSureStop() {
		this( DEFAULT_WAIT_DELAY );
	}

	/**
	 * Creates a new shutdown hook that waits the given amount of time for
	 * the JVM to exit cleanly before halting it.
	 *
	 * @param delay The amount of time in milliseconds to wait for the JVM
	 *              to exit cleanly
	 */
	public JvmSureStop( int delay ) {
		super( "JVM Sure Stop" );
		this.delay = delay;
		setDaemon( true );
	}

	/**
	 * Waits for the given amount of time for the JVM to exit cleanly. If the
	 * JVM has not exited, the running non-daemon threads and their stack
	 * traces are logged and the JVM is halted. In a test environment this
	 * method does nothing.
	 */
	@Override
	public void run() {
		try {
			if( TestUtil.isTest() ) return;

			try {
				Thread.sleep( delay );
			} catch( InterruptedException exception ) {
				return;
			}

			log.atSevere().log( "JVM did not exit cleanly. Here are the running non-daemon threads:" );
			Map<Thread, StackTraceElement[]> threadStacks = Thread.getAllStackTraces();
			threadStacks.keySet().stream().filter( t -> !t.isDaemon() ).forEach( thread -> {
				log.atWarning().log( "Thread: %s %s[g=%s]", thread.threadId(), thread.getName(), thread.getThreadGroup() );
				Arrays.stream( threadStacks.get( thread ) ).forEach( e -> log.atInfo().log( "  %s", e ) );
			} );

			log.atSevere().log( "Halting now!" );
			ThreadUtil.pause( 200 );

			Runtime.getRuntime().halt( -1 );
		} catch( Throwable throwable ) {
			log.atSevere().withCause( throwable ).log( "Error running %s", getClass().getSimpleName() );
		}
	}

}
