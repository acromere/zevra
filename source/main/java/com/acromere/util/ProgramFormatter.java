package com.acromere.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * The ProgramFormatter class is a custom formatter for log records that
 * formats the log messages according to a specific format. It extends the
 * {@link Formatter} class provided by the Java logging framework. The format
 * of the log messages can be customized by setting the
 * {@value FORMAT_PROPERTY_KEY} property in the logging configuration. If no
 * format is specified, the default format is used.
 * <p>
 * The format of the log messages is specified using a format string
 * containing placeholders for the different log record attributes. The
 * supported placeholders are:<br>
 * - %1$tF: The date of the log record formatted as "yyyy-MM-dd".<br>
 * - %1$tT: The time of the log record formatted as "HH:mm:ss".<br>
 * - %1$tL: The milliseconds of the log record.<br>
 * - %2$s: The source of the log record (the short class name and method
 * name, or the logger name if the source class name is not specified).<br>
 * - %3$s: The logger name.<br>
 * - %4$s: The abbreviated log level, for example <code>[E]</code> for severe
 * and <code>[I]</code> for info.<br>
 * - %5$s: The log message.<br>
 * - %6$s: The stack trace of the thrown exception, or an empty string if no
 * exception is present.<br>
 * - %n: A platform-dependent line separator.
 * <p>
 * Example usage:
 * <pre>{@code
 *   Logger logger = Logger.getLogger("myLogger");
 *   Handler handler = new ConsoleHandler();
 *   handler.setFormatter(new ProgramFormatter());
 *   logger.addHandler(handler);
 *   logger.log(Level.INFO, "This is a log message.");
 * }</pre>
 */
public class ProgramFormatter extends Formatter {

	private static final String FORMAT_PROPERTY_KEY = ProgramFormatter.class.getName() + ".format";

	private static final String DEFAULT_FORMAT = "%1$tF %1$tT.%1$tL %4$s %2$s %5$s %6$s%n";

	private final String format = getSimpleFormat( ProgramFormatter::getLoggingProperty );

	/**
	 * Retrieves the simple format string for log messages.
	 *
	 * @param defaultPropertyGetter a function that retrieves a property value based on a key
	 * @return the simple format string for log messages
	 */
	private static String getSimpleFormat( Function<String, String> defaultPropertyGetter ) {
		String format = defaultPropertyGetter.apply( FORMAT_PROPERTY_KEY );
		if( format == null ) format = getLoggingProperty( FORMAT_PROPERTY_KEY );
		if( format == null ) format = DEFAULT_FORMAT;
		return format;
	}

	/**
	 * Retrieves the value of the logging property with the given name.
	 *
	 * @param name the name of the logging property
	 * @return the value of the logging property
	 */
	private static String getLoggingProperty( String name ) {
		return LogManager.getLogManager().getProperty( name );
	}

	/**
	 * Formats the given LogRecord into a formatted log message.
	 *
	 * @param record the LogRecord to format
	 * @return the formatted log message
	 */
	@Override
	public String format( LogRecord record ) {
		// Timestamp
		Instant instant = Instant.ofEpochMilli( record.getMillis() );
		ZonedDateTime timestamp = ZonedDateTime.ofInstant( instant, ZoneId.systemDefault() );

		// Source
		String source = record.getSourceClassName();
		if( source == null ) {
			source = record.getLoggerName();
		} else {
			source = JavaUtil.getShortClassName( source );
			if( record.getSourceMethodName() != null ) source += "." + record.getSourceMethodName();
		}

		// Logger
		String logger = record.getLoggerName();

		// Level
		String level = getLevel( record.getLevel() );
		//String level = record.getLevel().getLocalizedName();

		// Message
		String message = formatMessage( record );

		// Throwable
		String throwable = "";
		if( record.getThrown() != null ) {
			StringWriter writer = new StringWriter();
			PrintWriter printer = new PrintWriter( writer );
			printer.println();
			record.getThrown().printStackTrace( printer );
			printer.close();
			throwable = writer.toString();
		}

		return String.format( format, timestamp, source, logger, level, message, throwable );
	}

	/**
	 * Returns the level abbreviation for the given Level object.
	 *
	 * @param level the Level object to get the abbreviation for
	 * @return the level abbreviation as a String
	 */
	private String getLevel( Level level ) {
		String result;

		switch( level.getName() ) {
			case ("SEVERE"): {
				result = "[E]";
				break;
			}
			case ("WARNING"): {
				result = "[W]";
				break;
			}
			case ("INFO"): {
				result = "[I]";
				break;
			}
			case ("CONFIG"): {
				result = "[C]";
				break;
			}
			case ("FINE"): {
				result = "[D]";
				break;
			}
			case ("FINER"): {
				result = "[T]";
				break;
			}
			default: {
				result = "[" + level.getName().charAt( 0 ) + "]";
			}
		}

		return result;
	}

}
