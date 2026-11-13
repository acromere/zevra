package com.acromere.transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * The results of a completed transaction operation. These results are collected
 * as part of the commit operation, and the events are distributed when the
 * commit is successful. Results may contain zero or more {@link TxnEvent} objects
 * that will be dispatched to their respective {@link TxnEventTarget targets}.
 */
public class TxnOperationResult {

	private final TxnOperation action;

	private final List<TxnEventWrapper> events = new ArrayList<>();

	TxnOperationResult( TxnOperation action ) {
		this.action = action;
	}

	/**
	 * Get the operation that produced this result.
	 *
	 * @return The operation
	 */
	public TxnOperation getOperation() {
		return action;
	}

	/**
	 * Add an event to be dispatched when the transaction commits successfully.
	 *
	 * @param target The target to dispatch the event to
	 * @param event  The event to dispatch
	 */
	public void addEvent( TxnEventTarget target, TxnEvent event ) {
		events.add( new TxnEventWrapper( target, event ) );
	}

	/**
	 * Copy all events from another operation's result into this result.
	 *
	 * @param operation The operation to copy events from
	 */
	public void addEventsFrom( TxnOperation operation ) {
		this.events.addAll( operation.getResult().getEvents() );
	}

	List<TxnEventWrapper> getEvents() {
		return events;
	}

}
