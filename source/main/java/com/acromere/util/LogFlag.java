package com.acromere.util;

import java.util.logging.Level;

public interface LogFlag {

	/**
	 * Turn the file log append on. The default is off.
	 */
	String LOG_APPEND = "--log-append";

	/**
	 * Valid values are:
	 * <li>off</li>
	 * <li>error</li>
	 * <li>warn</li>
	 * <li>info</li>
	 * <li>debug</li>
	 * <li>trace</li>
	 * <li>all</li>
	 */
	String LOG_LEVEL = "--log-level";

	/**
	 * The log file pattern according to the {@link java.util.logging.FileHandler}.
	 */
	String LOG_FILE = "--log-file";

	String NONE = "none";

	String ERROR = "error";

	String WARN = "warn";

	String INFO = "info";

	String CONFIG = "config";

	String DEBUG = "debug";

	String TRACE = "trace";

	String ALL = "all";

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
