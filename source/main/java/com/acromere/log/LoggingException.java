package com.acromere.log;

/**
 * Exception thrown to indicate errors that should be propagated from the
 * logging layer into user code.
 * <p>
 * This exception is primarily used when a logger provider chooses to
 * propagate an error rather than handle it silently. See
 * {@link com.acromere.log.provider.LoggerWrapper#handleError(LoggerWrapper, RuntimeException) handleError}
 * for details on when this is appropriate.
 */
public class LoggingException extends RuntimeException {

	/**
	 * Creates a logging exception with the specified message.
	 *
	 * @param message the error message
	 */
	public LoggingException( String message ) {
		super( message );
	}

	/**
	 * Creates a logging exception with the specified message and cause.
	 *
	 * @param message the error message
	 * @param cause the underlying cause
	 */
	public LoggingException( String message, Throwable cause ) {
		super( message, cause );
	}

}
