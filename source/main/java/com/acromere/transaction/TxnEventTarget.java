package com.acromere.transaction;

/**
 * Interface for objects that receive transaction events. Classes that need to
 * respond to transaction lifecycle events (such as commit begin, success, fail,
 * or end) should implement this interface and pass an instance to
 * {@link Txn#submit(TxnEventTarget, java.util.function.Consumer) Txn.submit()}.
 */
public interface TxnEventTarget {

	/**
	 * Dispatch a transaction event to this target.
	 *
	 * @param event The event to dispatch
	 */
	void dispatch( TxnEvent event );

}
