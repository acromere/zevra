package com.acromere.transaction;

/**
 * Checked exception thrown when a transaction operation fails.
 * <p>
 * This exception is thrown by {@link Txn#submit(TxnOperation) Txn.submit()}
 * if there is no active transaction, and by transaction operations during
 * commit or revert if they encounter errors.
 */
public class TxnException extends Exception {

	/**
	 * Create a transaction exception with no message or cause.
	 */
	public TxnException() {
		super();
	}

	/**
	 * Create a transaction exception with the specified message.
	 *
	 * @param message The error message
	 */
	public TxnException( String message ) {
		super( message );
	}

	/**
	 * Create a transaction exception with the specified message and cause.
	 *
	 * @param message The error message
	 * @param cause   The underlying cause
	 */
	public TxnException( String message, Throwable cause ) {
		super( message, cause );
	}

	/**
	 * Create a transaction exception with the specified cause.
	 *
	 * @param cause The underlying cause
	 */
	public TxnException( Throwable cause ) {
		super( cause );
	}

}
