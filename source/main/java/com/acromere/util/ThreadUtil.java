package com.acromere.util;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * This class provides utility methods related to thread operations.
 */
public class ThreadUtil {

	/**
	 * Pause a thread for a specific amount of time. If an InterruptedException
	 * occurs, the method returns immediately.
	 *
	 * @param duration The pause duration in milliseconds
	 */
	public static void pause( long duration ) {
		pause( duration, TimeUnit.MILLISECONDS );
	}

	/**
	 * Pause a thread for a specific amount of time. The duration is always
	 * interpreted as milliseconds, so the unit parameter does not change the
	 * length of the pause. If an InterruptedException occurs, the method
	 * returns immediately.
	 *
	 * @param duration The pause duration in milliseconds
	 * @param unit The time unit, which does not affect the length of the pause
	 */
	public static void pause( long duration, TimeUnit unit ) {
		try {
			unit.sleep( unit.convert( duration, TimeUnit.MILLISECONDS ) );
		} catch( InterruptedException exception ) {
			// Intentionally ignore exception.
		}
	}

	/**
	 * Creates a daemon thread with the given runnable. The thread is set as a
	 * daemon thread, meaning it will not prevent the JVM from exiting if it is
	 * the only thread running.
	 *
	 * @param runnable The runnable to be executed by the thread.
	 * @return The created daemon thread.
	 */
	public static Thread asDaemon( Runnable runnable ) {
		Thread thread = new Thread( runnable );
		thread.setDaemon( true );
		return thread;
	}

	/**
	 * Creates a daemon {@link ThreadFactory}.
	 *
	 * @return A ThreadFactory that creates daemon threads.
	 */
	public static ThreadFactory createDaemonThreadFactory() {
		return new DaemonThreadFactory();
	}

	/**
	 * Check if the specified class and method are present in the current
	 * thread's stack trace. Class names are compared both by the simple name
	 * and by the fully qualified name. For example, both of the following
	 * calls will return true:
	 * <p>
	 * <blockquote>
	 * <code>ThreadUtil.calledFrom( "Thread", "run" );</code><br/>
	 * <code>ThreadUtil.calledFrom( "java.lang.Thread", "run" );</code>
	 * </blockquote>
	 *
	 * @param className The name of the calling class
	 * @param methodName The name of the calling method
	 * @return true if the class and method match the calling class and method
	 */
	public static boolean calledFrom( String className, String methodName ) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();

		for( StackTraceElement element : trace ) {
			if( element.getClassName().equals( className ) && element.getMethodName().equals( methodName ) ) return true;
			// This only compares class name and method name, not the package
			if( JavaUtil.getClassName( element.getClassName() ).equals( JavaUtil.getClassName( className ) ) && element.getMethodName().equals( methodName ) ) return true;
		}

		return false;
	}

	/**
	 * Append the stack trace of the source throwable to the target throwable.
	 *
	 * @param source The source throwable
	 * @param target The target throwable
	 * @return The target throwable with the stack trace appended, the source
	 * throwable if the target is null, or null if both are null
	 */
	public static Throwable appendStackTrace( Throwable source, Throwable target ) {
		if( source == null ) return target;
		if( target == null ) return source;
		return appendStackTrace( source.getStackTrace(), target );
	}

	/**
	 * Append the stack trace to the target throwable.
	 *
	 * @param trace The stack trace to append, ignored if null
	 * @param target The target throwable
	 * @return The target throwable with the stack trace appended, or null if
	 * the target is null
	 */
	public static Throwable appendStackTrace( StackTraceElement[] trace, Throwable target ) {
		if( target == null ) return null;
		if( trace != null ) {
			StackTraceElement[] originalStack = target.getStackTrace();
			StackTraceElement[] elements = new StackTraceElement[ originalStack.length + trace.length ];
			System.arraycopy( originalStack, 0, elements, 0, originalStack.length );
			System.arraycopy( trace, 0, elements, originalStack.length, trace.length );
			target.setStackTrace( elements );
		}
		return target;
	}

	/**
	 * Returns the current execution stack as an array of class names. This
	 * is useful to determine the calling class.
	 * <p>
	 * The length of the array is the number of methods on the execution stack
	 * before this method is called. The element at index 0 is the class that
	 * called this method, the element at index 1 is the class that called
	 * the method at index 0, and so on.
	 *
	 * @return An array of class names of the execution stack before this
	 * method was called.
	 */
	public static String[] getStackClasses() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		String[] frame = Arrays.stream( elements ).map( StackTraceElement::getClassName ).toList().toArray( new String[ elements.length ] );
		return Arrays.copyOfRange( frame, 2, frame.length );
	}

	/**
	 * Prints the names of all currently running threads in the Java application.
	 * <p>
	 * This method retrieves all running threads using the {@link Thread#getAllStackTraces()}
	 * method and prints the name of each thread to the console.
	 */
	public static void printRunningThreads() {
		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		for( Thread thread : traces.keySet() ) {
			System.out.println( thread.getName() );
		}
	}

	/**
	 * A {@link ThreadFactory} implementation that creates daemon threads.
	 */
	private static final class DaemonThreadFactory implements ThreadFactory {

		public Thread newThread( @NonNull Runnable runnable ) {
			Thread thread = Executors.defaultThreadFactory().newThread( runnable );
			thread.setDaemon( true );
			return thread;
		}

	}

}
