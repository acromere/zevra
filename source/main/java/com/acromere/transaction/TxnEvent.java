package com.acromere.transaction;

import com.acromere.event.Event;
import com.acromere.event.EventType;

/**
 * Transaction lifecycle events. These events are dispatched to
 * {@link TxnEventTarget targets} during the transaction commit process
 * to notify them of transaction state changes.
 * <p>
 * The event hierarchy uses the {@link #ANY} type as the root, with
 * specific lifecycle events as children.
 */
public class TxnEvent extends Event {

	/** Root event type for all transaction events. */
	public static final EventType<TxnEvent> ANY = new EventType<>( EventType.ROOT, "TXN" );

	/** Dispatched at the start of a transaction commit. */
	public static final EventType<TxnEvent> COMMIT_BEGIN = new EventType<>( ANY, "COMMIT_BEGIN" );

	/** Dispatched when a transaction commit completes successfully. */
	public static final EventType<TxnEvent> COMMIT_SUCCESS = new EventType<>( ANY, "COMMIT_SUCCESS" );

	/** Dispatched when a transaction commit fails. */
	public static final EventType<TxnEvent> COMMIT_FAIL = new EventType<>( ANY, "COMMIT_FAIL" );

	/** Dispatched when a transaction commit completes, regardless of success or failure. */
	public static final EventType<TxnEvent> COMMIT_END = new EventType<>( ANY, "COMMIT_END" );

	/**
	 * Create a TxnEvent where the source and target are the same object. This is
	 * a common pattern where the eventual target of the event is the same object
	 * that is creating it.
	 *
	 * @param source The event source/target
	 */
	public TxnEvent( TxnEventTarget source, EventType<? extends TxnEvent> type ) {
		super( source, type );
	}

	/**
	 * Determine how duplicate events should be collapsed during dispatch.
	 * If true, the first instance of a duplicate event is kept and subsequent
	 * duplicates are discarded. If false, the last instance is kept.
	 *
	 * @return true to keep the first instance, false to keep the last
	 */
	public boolean collapseUp() {
		return false;
	}

}
