package com.acromere.log;

import com.acromere.log.java.JavaLoggingProvider;
import com.acromere.log.provider.LoggingProvider;

/**
 * Factory class for creating {@link Logger} instances and managing
 * the active {@link LoggingProvider}.
 * <p>
 * By default, the {@link JavaLoggingProvider} is used, which wraps
 * Java's built-in {@code java.util.logging} framework.
 */
public class Logging {

	private static LoggingProvider provider = new JavaLoggingProvider();

	/**
	 * Returns the current {@link LoggingProvider} instance.
	 */
	public static LoggingProvider getLoggingProvider() {
		return provider;
	}

	/**
	 * Sets the {@link LoggingProvider} to be used for all subsequent
	 * logger instances created by this factory.
	 *
	 * @param provider the logging provider to use
	 */
	public static void setLoggingProvider( LoggingProvider provider ) {
		Logging.provider = provider;
	}

	/**
	 * Creates a new {@link Logger} instance for the given class.
	 *
	 * @param clazz the class for which to create a logger; its name will be used as the logger name
	 * @return a new logger instance
	 */
	public static Logger create( java.lang.Class<?> clazz ) {
		return new Logger( getLoggingProvider().getLoggerWrapper( clazz.getName() ) );
	}

}
