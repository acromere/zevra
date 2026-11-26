package com.acromere.log.java;

import com.acromere.log.LogData;
import com.acromere.log.provider.LoggerWrapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A {@link LoggerWrapper} implementation that wraps Java's {@code java.util.logging.Logger}.
 * <p>
 * This wrapper converts Zevra log data into {@link LogRecord} instances that are
 * passed to the underlying Java logger for output.
 */
public class JavaLoggingLoggerWrapper implements LoggerWrapper {

	/**
	 * The number of nanoseconds in one second.
	 */
	private static final long ONE_BILLION = 1000000000;

	/**
	 * The Java logger instance.
	 */
	private final Logger logger;

	/**
	 * Creates a logger wrapper around the given Java logger.
	 *
	 * @param logger the Java logger to wrap
	 */
	public JavaLoggingLoggerWrapper( java.util.logging.Logger logger ) {
		this.logger = logger;
	}

	/**
	 * Returns the name of the wrapped Java logger.
	 *
	 * @return the logger name, or {@code null} if not given
	 */
	@Override
	public String getLoggerName() {
		return logger.getName();
	}

	/**
	 * Returns whether logging is enabled for the given level.
	 *
	 * @param level the log level to check
	 * @return {@code true} if logging is enabled for the given level
	 */
	@Override
	public boolean isLoggable( Level level ) {
		return logger.isLoggable( level );
	}

	/**
	 * Outputs the log statement represented by the given {@link LogData} instance
	 * by passing a {@link LogRecord} to the underlying Java logger.
	 *
	 * @param data the log data to output
	 */
	@Override
	public void log( LogData data ) {
		LogRecord record = new LogRecord( data.getLevel(), null );

		record.setSourceClassName( (String)data.getMetadata().get( LogData.CLASS_NAME ) );
		record.setSourceMethodName( (String)data.getMetadata().get( LogData.METHOD_NAME ) );
		record.setInstant( Instant.ofEpochSecond( data.getTimestampNanos() / ONE_BILLION, data.getTimestampNanos() % ONE_BILLION ) );
		record.setMessage( data.getMessage() );
		record.setParameters( data.getMessageParameters() );
		record.setThrown( (Throwable)data.getMetadata().get( LogData.CAUSE ) );

		logger.log( record );
	}

	/**
	 * Handles an error in a log statement by printing the error's stack trace.
	 *
	 * @param data the original {@link LogData} instance which caused the error
	 * @param error the exception thrown when the log data was initially logged
	 */
	@Override
	public void handleError( LogData data, RuntimeException error ) {
		error.printStackTrace();
	}

	/**
	 * Flushes any buffered log data by flushing all handlers of the wrapped Java logger.
	 */
	public void flush() {
		Arrays.stream( logger.getHandlers() ).forEach( Handler::flush );
	}

}
