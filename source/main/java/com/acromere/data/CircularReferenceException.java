package com.acromere.data;

import java.io.Serial;

/**
 * Unchecked exception thrown when a circular reference is detected in the
 * parent path of a {@link DataNode}.
 * <p>
 * This exception is thrown if setting a parent on a node would create a
 * cycle in the parent path of the node.
 */
public class CircularReferenceException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 8169280314381710751L;

	/**
	 * Create a circular reference exception with no message or cause.
	 */
	public CircularReferenceException() {
		super();
	}

	/**
	 * Create a circular reference exception with the specified message.
	 *
	 * @param message The error message
	 */
	public CircularReferenceException( String message ) {
		super( message );
	}

	/**
	 * Create a circular reference exception with the specified cause.
	 *
	 * @param cause The underlying cause
	 */
	public CircularReferenceException( Throwable cause ) {
		super( cause );
	}

	/**
	 * Create a circular reference exception with the specified message and
	 * cause.
	 *
	 * @param message The error message
	 * @param cause   The underlying cause
	 */
	public CircularReferenceException( String message, Throwable cause ) {
		super( message, cause );
	}

}
