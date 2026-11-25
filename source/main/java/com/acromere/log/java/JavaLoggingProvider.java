package com.acromere.log.java;

import com.acromere.log.provider.AbstractLoggingProvider;
import com.acromere.log.provider.LoggerWrapper;

/**
 * Logging provider that wraps Java's built-in {@code java.util.logging} framework.
 * <p>
 * This is the default logging provider used by the Zevra logging system.
 */
public class JavaLoggingProvider extends AbstractLoggingProvider {

	/**
	 * Creates a logger wrapper for the given logger name.
	 *
	 * @param name the logger name
	 * @return a new {@link JavaLoggingLoggerWrapper} wrapping the Java logger with the given name
	 */
	@Override
	public LoggerWrapper getLoggerWrapper( String name ) {
		return new JavaLoggingLoggerWrapper( java.util.logging.Logger.getLogger( name ) );
	}

}
