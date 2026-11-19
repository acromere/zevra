package com.acromere.index;

/**
 * Unchecked exception thrown when an {@link Index} is not found.
 */
public class IndexNotFoundException extends RuntimeException {

	/**
	 * Create an index not found exception with no message or cause.
	 */
	public IndexNotFoundException() {
		super();
	}

	/**
	 * Create an index not found exception with the specified message.
	 *
	 * @param message The error message
	 */
	public IndexNotFoundException( String message ) {
		super( message );
	}

	/**
	 * Create an index not found exception with the specified message and
	 * cause.
	 *
	 * @param message The error message
	 * @param cause   The underlying cause
	 */
	public IndexNotFoundException( String message, Throwable cause ) {
		super( message, cause );
	}

	/**
	 * Create an index not found exception with the specified cause.
	 *
	 * @param cause The underlying cause
	 */
	public IndexNotFoundException( Throwable cause ) {
		super( cause );
	}
}
