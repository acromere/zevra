/**
 * Basic, multipurpose, thread-scoped transaction management. Originally
 * developed as part of the {@link com.acromere.data} package, the logic was
 * eventually separated for other uses. The general intent is that things that
 * should happen together happen as a unit when the transaction is committed.
 * <p>
 * Transactions are thread-local for convenience since most transactions are
 * created and committed on the same thread. Creating a new transaction will
 * append to an existing transaction unless the transaction is explicitly
 * created as a nested transaction.
 * <p>
 * Core classes include:
 * <ul>
 *   <li>{@link com.acromere.transaction.Txn Txn} - The main transaction utility for creating, submitting, and committing transactions</li>
 *   <li>{@link com.acromere.transaction.TxnOperation TxnOperation} - Abstract class for individual transaction operations that are processed during commit</li>
 *   <li>{@link com.acromere.transaction.TxnOperationResult TxnOperationResult} - The results of a completed transaction operation, including events to dispatch</li>
 *   <li>{@link com.acromere.transaction.TxnEvent TxnEvent} - Transaction lifecycle events (COMMIT_BEGIN, COMMIT_SUCCESS, COMMIT_FAIL, COMMIT_END)</li>
 *   <li>{@link com.acromere.transaction.TxnEventTarget TxnEventTarget} - Interface for objects that receive transaction events</li>
 *   <li>{@link com.acromere.transaction.TxnException TxnException} - Checked exception for transaction failures</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 *   Txn.create();
 *   Txn.submit( new SetX( x ) );
 *   Txn.submit( new SetY( y ) );
 *   Txn.commit();
 * </pre>
 * Or using try-with-resources for automatic commit:
 * <pre>
 *   try( Txn ignored = Txn.create() ) {
 *       // perform operations
 *   }
 * </pre>
 */
package com.acromere.transaction;