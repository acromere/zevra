package com.acromere.log.provider;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Interface for logging providers that create logger wrappers.
 * <p>
 * A logging provider is responsible for creating {@link LoggerWrapper} instances
 * that interface with an underlying logging implementation. Different providers
 * can be swapped to use different logging backends.
 */
public interface LoggingProvider {

	/**
	 * Returns the current system time as nanoseconds.
	 *
	 * @return the current time in nanoseconds
	 */
	static long getCurrentTimeNanos() {
		return MILLISECONDS.toNanos( System.currentTimeMillis() );
	}

	/**
	 * Creates a logger wrapper for the given logger name.
	 *
	 * @param name the logger name
	 * @return a new logger wrapper
	 */
	LoggerWrapper getLoggerWrapper( String name );

}
