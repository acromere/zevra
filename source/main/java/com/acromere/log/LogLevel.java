package com.acromere.log;

import java.util.logging.Level;

/**
 * Standard logging levels for the Zevra logging framework.
 * <p>
 * This class defines the canonical log levels used throughout the logging API.
 * It extends {@link java.util.logging.Level} to provide compatibility with
 * Java's built-in logging infrastructure while maintaining the Zevra naming convention.
 * <p>
 * The severity ordering from least to most severe is:
 * {@code TRACE} &lt; {@code DEBUG} &lt; {@code INFO} &lt; {@code WARN} &lt; {@code ERROR}.
 *
 * @see Logger
 * @see Log
 */
public class LogLevel extends Level {

	/**
	 * A special log level to suppress all log output.
	 * Equivalent to {@link Level#OFF}.
	 */
	public static final LogLevel NONE = new LogLevel( "NONE", Level.OFF.intValue() );

	/**
	 * Level for error conditions that prevent normal operation.
	 * Equivalent to {@link Level#SEVERE}.
	 */
	public static final LogLevel ERROR = new LogLevel( "ERROR", Level.SEVERE.intValue() );

	/**
	 * Level for warning conditions that may lead to problems.
	 * Equivalent to {@link Level#WARNING}.
	 */
	public static final LogLevel WARN = new LogLevel( "WARN", Level.WARNING.intValue() );

	/**
	 * Level for informational messages about normal program operation.
	 * Equivalent to {@link Level#INFO}.
	 */
	public static final LogLevel INFO = new LogLevel( "INFO", Level.INFO.intValue() );

	/**
	 * Level for detailed debug information useful during development.
	 * Equivalent to {@link Level#FINE}.
	 */
	public static final LogLevel DEBUG = new LogLevel( "DEBUG", Level.FINE.intValue() );

	/**
	 * Level for the finest-grained trace information.
	 * Equivalent to {@link Level#FINEST}.
	 */
	public static final LogLevel TRACE = new LogLevel( "TRACE", Level.FINEST.intValue() );

	/**
	 * A special log level to enable all log output.
	 * Equivalent to {@link Level#ALL}.
	 */
	public static final LogLevel ALL = new LogLevel( "ALL", Level.ALL.intValue() );

	protected LogLevel( String name, int value ) {
		super( name, value );
	}

	protected LogLevel( String name, int value, String resourceBundleName ) {
		super( name, value, resourceBundleName );
	}

}
