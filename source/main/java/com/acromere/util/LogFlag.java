package com.acromere.util;

import java.util.logging.Level;

/**
 * Constants for logging related command line arguments and log levels.
 */
public interface LogFlag {

	/**
	 * Turn the file log append on. The default is off.
	 */
	String LOG_APPEND = "--log-append";

	/**
	 * Valid values are:
	 * <ul>
	 * <li>none</li>
	 * <li>error</li>
	 * <li>warn</li>
	 * <li>info</li>
	 * <li>config</li>
	 * <li>debug</li>
	 * <li>trace</li>
	 * <li>all</li>
	 * </ul>
	 *
	 * <p>Unknown values are treated as {@link #NONE}.</p>
	 */
	String LOG_LEVEL = "--log-level";

	/**
	 * The log file pattern according to the {@link java.util.logging.FileHandler}.
	 */
	String LOG_FILE = "--log-file";

	/**
	 * The log level that turns all logging off.
	 */
	String NONE = "none";

	/**
	 * The log level for error messages.
	 */
	String ERROR = "error";

	/**
	 * The log level for warning messages.
	 */
	String WARN = "warn";

	/**
	 * The log level for informational messages.
	 */
	String INFO = "info";

	/**
	 * The log level for configuration messages.
	 */
	String CONFIG = "config";

	/**
	 * The log level for debug messages.
	 */
	String DEBUG = "debug";

	/**
	 * The log level for trace messages.
	 */
	String TRACE = "trace";

	/**
	 * The log level that enables all logging.
	 */
	String ALL = "all";

	/**
	 * Converts a log level name to the corresponding {@link Level}. The
	 * match is case insensitive.
	 *
	 * @param level The log level name, or {@code null}
	 * @return The corresponding {@link Level}, or {@link Level#OFF} if the
	 *         level is {@code null} or unknown
	 */
	static Level toLogLevel( String level ) {
		return switch( level == null ? LogFlag.NONE : level.toLowerCase() ) {
			case LogFlag.ERROR -> Level.SEVERE;
			case LogFlag.WARN -> Level.WARNING;
			case LogFlag.INFO -> Level.INFO;
			case LogFlag.CONFIG -> Level.CONFIG;
			case LogFlag.DEBUG -> Level.FINE;
			case LogFlag.TRACE -> Level.FINEST;
			case LogFlag.ALL -> Level.ALL;
			default -> Level.OFF;
		};
	}

}
