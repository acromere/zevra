package com.acromere.log;

import com.acromere.log.provider.LoggerWrapper;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Abstract base class for loggers.
 * <p>
 * Provides common functionality for logger implementations, including
 * convenience methods for logging at specific levels (e.g., {@link #atInfo()},
 * {@link #atDebug()}, {@link #atError(Throwable)}).
 * <p>
 * Subclasses must implement {@link #at(Level)} to create logging contexts.
 *
 * @param <API> the specific API type for fluent method chaining
 */
public abstract class AbstractLogger<API extends LogApi<API>> {

	private final LoggerWrapper provider;

	/**
	 * Creates an abstract logger backed by the given provider.
	 *
	 * @param provider the logger wrapper providing the logging backend
	 */
	protected AbstractLogger( LoggerWrapper provider ) {
		this.provider = Objects.requireNonNull( provider );
	}

	/**
	 * Creates a logging context for the given log level.
	 *
	 * @param level the log level for the log statement
	 * @return the fluent API for building the log statement, or a no-op API if the level is not loggable
	 */
	public abstract API at( Level level );

	// Java Logging convenience methods ------------------------------------------

	/**
	 * A convenience method for at({@link Level#SEVERE}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atSevere() {
		return at( Level.SEVERE );
	}

	/**
	 * A convenience method for at({@link Level#SEVERE}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atError() {
		return at( Level.SEVERE );
	}

	/**
	 * A convenience method for at({@link Level#SEVERE}).
	 *
	 * @param cause the exception to associate with the log statement
	 * @return the fluent API for the log statement
	 */
	public final API atError( Throwable cause ) {
		return at( Level.SEVERE ).withCause( cause );
	}

	/**
	 * A convenience method for at({@link Level#WARNING}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atWarning() {
		return at( Level.WARNING );
	}

	/**
	 * A convenience method for at({@link Level#WARNING}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atWarn() {
		return at( Level.WARNING );
	}

	/**
	 * A convenience method for at({@link Level#WARNING}).
	 *
	 * @param cause the exception to associate with the log statement
	 * @return the fluent API for the log statement
	 */
	public final API atWarn( Throwable cause ) {
		return at( Level.WARNING ).withCause( cause );
	}

	/**
	 * A convenience method for at({@link Level#INFO}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atInfo() {
		return at( Level.INFO );
	}

	/**
	 * A convenience method for at({@link Level#CONFIG}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atConfig() {
		return at( Level.CONFIG );
	}

	/**
	 * A convenience method for at({@link Level#FINE}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atFine() {
		return at( Level.FINE );
	}

	/**
	 * A convenience method for at({@link Level#FINE}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atDebug() {
		return at( Level.FINE );
	}

	/**
	 * A convenience method for at({@link Level#FINER}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atFiner() {
		return at( Level.FINER );
	}

	/**
	 * A convenience method for at({@link Level#FINER}).
	 *
	 * @return the fluent API for the log statement
	 */
	public final API atTrace() {
		return at( Level.FINER );
	}

	/**
	 * A convenience method for at({@link Level#FINEST}).
	 *
	 * @return the fluent API for the log statement
	 */
	@SuppressWarnings( "unused" )
	public final API atFinest() {
		return at( Level.FINEST );
	}

	/**
	 * Flushes any buffered log data.
	 */
	public void flush() {
		provider.flush();
	}

	/**
	 * Returns the name of this logger.
	 *
	 * @return the logger name
	 */
	protected String getName() {
		return provider.getLoggerName();
	}

	/**
	 * Returns whether the given level is enabled for this logger. Users wishing to guard code with a
	 * check for "loggability" should use {@code logger.at(level).isEnabled()} instead.
	 *
	 * @param level the log level to check
	 * @return {@code true} if the level is enabled for this logger
	 */
	protected final boolean isLoggable( Level level ) {
		return provider.isLoggable( level );
	}

	final LoggerWrapper getProvider() {
		return provider;
	}

	final void write( LogData data ) {
		Objects.requireNonNull( data );
		try {
			provider.log( data );
		} catch( RuntimeException error ) {
			try {
				provider.handleError( data, error );
			} catch( LoggingException allowed ) {
				// Bypass the catch-all if the exception is deliberately created during error handling.
				throw allowed;
			} catch( RuntimeException runtimeException ) {
				System.err.println( "logging error: " + runtimeException.getMessage() );
				runtimeException.printStackTrace( System.err );
			}
		}
	}

}
