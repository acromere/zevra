package com.acromere.transaction;

import com.acromere.event.EventType;
import com.acromere.skill.Actionable;
import lombok.CustomLog;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Txn is a transaction utility. Transactions are thread local for convenience
 * since most transactions are created and committed on the same thread.
 * Creating a new transaction will append to an existing transaction unless the
 * transaction is explicitly created as a nested transaction. Example:
 * <pre>
 *   ...
 *   public void setPoint( double x, double y ) {
 *     // Note that SetX and SetY are implementations of TxnOperation
 *     Txn.create();
 *     Txn.submit( new SetX( x ) );
 *     Txn.submit( new SetY( y ) );
 *     Txn.commit();
 *   }
 *   ...
 * </pre>
 */
@CustomLog
public class Txn implements AutoCloseable {

	/**
	 * The stack of transactions for the current thread. The head of the
	 * deque is the active transaction.
	 */
	private static final ThreadLocal<Deque<Txn>> transactions = new ThreadLocal<>();

	static {
		transactions.set( new ArrayDeque<>() );
	}

	/**
	 * Lock that serializes commits of this transaction.
	 */
	private final ReentrantLock commitLock = new ReentrantLock();

	/**
	 * The operations submitted to this transaction, in submission order.
	 */
	private final Queue<TxnOperation> operations;

	/**
	 * The number of outstanding create calls for this transaction that have
	 * not yet been completed by a commit or reset.
	 */
	private final AtomicInteger atomicDepth;

	/**
	 * Create a new transaction. Transactions are only created through
	 * {@link #create()} or {@link #create(boolean)}.
	 */
	private Txn() {
		operations = new ConcurrentLinkedQueue<>();
		atomicDepth = new AtomicInteger();
	}

	/**
	 * Execute the given actionable in a transaction.
	 *
	 * @param step The actionable to run
	 * @throws RuntimeException if the step fails; any checked exception thrown
	 *                          by the step is wrapped in a new
	 *                          {@code RuntimeException}
	 */
	public static void run( Actionable step ) {
		try( Txn ignored = Txn.create() ) {
			step.act();
		} catch( Throwable throwable ) {
			if( throwable instanceof RuntimeException ) {
				throw (RuntimeException)throwable;
			} else {
				throw new RuntimeException( "Transaction failure", throwable );
			}
		}
	}

	/**
	 * Execute the given Callable wrapped in a Txn.
	 *
	 * @param runnable The Callable to execute
	 */
	public static <T> T call( Callable<T> runnable ) throws Exception {
		try( Txn ignored = Txn.create() ) {
			return runnable.call();
		}
	}

	/**
	 * Create a transaction for this thread if there is not already an active
	 * transaction. If there is already an active transaction then the active
	 * transaction is returned, otherwise a new transaction is created and
	 * returned.
	 *
	 * @return The transaction
	 */
	public static Txn create() {
		return create( false );
	}

	/**
	 * Create a transaction for this thread if there is not already an active
	 * transaction or the nest flag is set to intentionally start a nested
	 * transaction instead of using an existing transaction.
	 * <p>
	 * Note that the nested transaction becomes the new active transaction until
	 * it is completed (committed or reset) and therefore nested transactions must
	 * be completed before the outer transaction can be completed.
	 *
	 * @param nest Set to true if this transaction should not be part of an
	 * existing transaction.
	 * @return The transaction
	 */
	public static Txn create( boolean nest ) {
		Txn transaction = peekTransaction();
		if( transaction == null || nest ) transaction = pushTransaction();
		transaction.incrementDepth();

		return transaction;
	}

	/**
	 * Submit an operation to the currently active transaction. The operation
	 * will be executed during the transaction's commit.
	 *
	 * @param operation The operation to submit
	 * @throws TxnException if there is no active transaction
	 */
	public static void submit( TxnOperation operation ) throws TxnException {
		verifyActiveTransaction().operations.offer( operation );
	}

	/**
	 * Submit a simple operation to the currently active transaction using a
	 * lambda expression. The consumer will be called during the transaction's
	 * commit, providing access to the event target.
	 *
	 * @param target   The event target for this operation
	 * @param consumer The operation to execute on commit
	 * @throws TxnException if there is no active transaction
	 */
	public static void submit( TxnEventTarget target, Consumer<TxnEventTarget> consumer ) throws TxnException {
		submit( new ConsumerTxnOperation( target, consumer ) );
	}

	/**
	 * Commit the currently active transaction. All submitted operations will
	 * be executed in the order they were submitted. If any operation fails,
	 * all previously completed operations will be reverted.
	 * <p>
	 * Transaction lifecycle events are dispatched at various points during
	 * the commit process: {@link TxnEvent#COMMIT_BEGIN},
	 * {@link TxnEvent#COMMIT_SUCCESS} or {@link TxnEvent#COMMIT_FAIL}, and
	 * finally {@link TxnEvent#COMMIT_END}.
	 *
	 * @throws TxnException if there is no active transaction or if commit fails
	 */
	public static void commit() throws TxnException {
		Txn transaction = verifyActiveTransaction();
		transaction.decrementDepth();
		if( transaction.isActive() ) return;
		try {
			transaction.doCommit();
		} finally {
			pullTransaction();
		}
	}

	/**
	 * Reset (discard) the currently active transaction without committing.
	 * All submitted operations will be abandoned. This is typically called
	 * when a transaction should not be committed, such as when an error
	 * occurs before the intended commit point.
	 */
	public static void reset() {
		Txn transaction = peekTransaction();
		if( transaction == null ) return;
		if( transaction.atomicDepth.decrementAndGet() > 0 ) return;

		while( transaction != null ) {
			transaction.doReset();
			transaction = pullTransaction();
		}
	}

	/**
	 * Get the currently active transaction, if any.
	 *
	 * @return The active transaction, or null if no transaction is active
	 */
	public static Txn getActiveTransaction() {
		return peekTransaction();
	}

	/**
	 * Get the currently active transaction, throwing an exception if there
	 * is no active transaction.
	 *
	 * @return The active transaction
	 * @throws TxnException if there is no active transaction
	 */
	private static Txn verifyActiveTransaction() throws TxnException {
		Txn transaction = peekTransaction();
		if( transaction == null ) throw new TxnException( "No active transaction" );
		return transaction;
	}

	/**
	 * Get the currently active transaction without removing it.
	 *
	 * @return The active transaction, or null if no transaction is active
	 */
	private static Txn peekTransaction() {
		Deque<Txn> deque = transactions.get();
		return deque == null ? null : deque.peekFirst();
	}

	/**
	 * Create a new transaction and push it onto the top of this thread's
	 * transaction stack, making it the active transaction.
	 *
	 * @return The new transaction
	 */
	private static Txn pushTransaction() {
		Deque<Txn> deque = transactions.get();
		if( deque == null ) transactions.set( deque = new ArrayDeque<>() );
		Txn transaction = new Txn();
		deque.offerFirst( transaction );
		return transaction;
	}

	/**
	 * Remove and return the currently active transaction from this thread's
	 * transaction stack.
	 *
	 * @return The removed transaction, or null if no transaction is active
	 */
	private static Txn pullTransaction() {
		Deque<Txn> deque = transactions.get();
		if( deque == null ) return null;
		Txn transaction = deque.pollFirst();
		if( deque.isEmpty() ) transactions.remove();
		return transaction;
	}

	/**
	 * Commit the transaction. This enables use with try-with-resources:
	 * <pre>
	 *   try( Txn ignored = Txn.create() ) {
	 *       // submit operations
	 *   }
	 * </pre>
	 *
	 * @throws TxnException if commit fails
	 */
	@Override
	public void close() throws TxnException {
		commit();
	}

	/**
	 * Determine if this transaction is still active, that is, if it has
	 * outstanding create calls that have not yet been completed by a commit
	 * or reset.
	 *
	 * @return true if the transaction is active, false otherwise
	 */
	boolean isActive() {
		return atomicDepth.get() > 0;
	}

	/**
	 * Increment the depth of this transaction.
	 */
	void incrementDepth() {
		atomicDepth.incrementAndGet();
	}

	/**
	 * Decrement the depth of this transaction.
	 */
	void decrementDepth() {
		atomicDepth.decrementAndGet();
	}

	/**
	 * Commit the transaction's operations, dispatching transaction
	 * lifecycle events and publishing the results of each operation.
	 *
	 * @throws TxnException if any operation fails to commit
	 */
	private void doCommit() throws TxnException {
		Set<TxnOperation> operations = new HashSet<>( this.operations );

		try {
			commitLock.lock();
			log.atFiner().log( "Txn %s locked by: %s", System.identityHashCode( this ), Thread.currentThread() );

			// Send a commit begin event to all unique targets
			sendEvent( TxnEvent.COMMIT_BEGIN, operations );

			// Process all the operations
			List<TxnOperationResult> operationResults = new ArrayList<>( processOperations() );

			// Go through each operation result and collect the events by target
			// This process also removes duplicate events and puts them in the correct order
			Map<TxnEventTarget, List<TxnEvent>> txnEvents = getTxnEventTargets( operationResults );

			// Dispatch the events to the targets
			txnEvents.forEach( ( target, events ) -> events.forEach( event -> {
				try {
					target.dispatch( event );
				} catch( Throwable throwable ) {
					log.atSevere().withCause( throwable ).log( "Error dispatching transaction event" );
				}
			} ) );

			sendEvent( TxnEvent.COMMIT_SUCCESS, operations );
		} catch( TxnException throwable ) {
			sendEvent( TxnEvent.COMMIT_FAIL, operations );
			throw throwable;
		} finally {
			sendEvent( TxnEvent.COMMIT_END, operations );
			doReset();
			commitLock.unlock();
			log.atFiner().log( "Txn %s unlocked by: %s", System.identityHashCode( this ), Thread.currentThread() );
		}
	}

	/**
	 * Collect the events generated by the committed operations, grouped by
	 * target, removing duplicate events.
	 *
	 * @param operationResults The results of the committed operations
	 * @return A map of event target to the events to be dispatched to it
	 */
	@NonNull
	private Map<TxnEventTarget, List<TxnEvent>> getTxnEventTargets( List<TxnOperationResult> operationResults ) {
		Map<TxnEventTarget, List<TxnEvent>> txnEvents = new HashMap<>();
		for( TxnOperationResult operationResult : operationResults ) {
			for( TxnEventWrapper wrapper : operationResult.getEvents() ) {
				TxnEventTarget target = wrapper.target();
				TxnEvent event = wrapper.event();
				List<TxnEvent> events = txnEvents.computeIfAbsent( target, k -> new ArrayList<>() );
				if( event.collapseUp() ) {
					// Collapse equal events to the first instance of the event
					int index = events.indexOf( event );
					if( index < 0 ) {
						events.add( event );
					} else {
						events.remove( index );
						events.add( index, event );
					}
				} else {
					// Collapse equal events to the last instance of the event
					events.remove( event );
					events.add( event );
				}
			}
		}
		return txnEvents;
	}

	/**
	 * Send an event to all unique targets.
	 *
	 * @param type The event type
	 */
	private void sendEvent( EventType<? extends TxnEvent> type, Collection<TxnOperation> operations ) {
		operations.stream().map( TxnOperation::getTarget ).distinct().forEach( t -> t.dispatch( new TxnEvent( t, type ) ) );
	}

	/**
	 * Execute all the submitted operations in the order they were
	 * submitted. If any operation fails, the previously completed
	 * operations are reverted.
	 *
	 * @return The results of the committed operations
	 * @throws TxnException if any operation fails
	 */
	private List<TxnOperationResult> processOperations() throws TxnException {
		// Process the operations.
		List<TxnOperationResult> operationResults = new ArrayList<>();
		List<TxnOperation> completedOperations = new ArrayList<>();
		try {
			TxnOperation operation;
			while( (operation = operations.poll()) != null ) {
				operationResults.add( operation.callCommit() );
				completedOperations.add( operation );
			}
		} catch( TxnException commitException ) {
			try {
				for( TxnOperation operation : completedOperations ) {
					if( operation.getStatus() == TxnOperation.Status.COMMITTED ) operation.callRevert();
				}
			} catch( TxnException rollbackException ) {
				throw new TxnException( "Error rolling back transaction", rollbackException );
			}
		}
		return operationResults;
	}

	/**
	 * Discard all the operations submitted to this transaction.
	 */
	private void doReset() {
		operations.clear();
	}

	/**
	 * A transaction operation that executes a consumer on commit. Used to
	 * support the lambda-based
	 * {@link Txn#submit(TxnEventTarget, java.util.function.Consumer) submit}
	 * method.
	 */
	private static class ConsumerTxnOperation extends TxnOperation {

		/**
		 * The operation to execute on commit.
		 */
		private final Consumer<TxnEventTarget> consumer;

		/**
		 * Create a consumer operation for the given target and consumer.
		 *
		 * @param target   The event target for this operation
		 * @param consumer The operation to execute on commit
		 */
		public ConsumerTxnOperation( TxnEventTarget target, Consumer<TxnEventTarget> consumer ) {
			super( target );
			this.consumer = consumer;
		}

		/**
		 * Execute the consumer with the operation's target.
		 *
		 * @return This operation
		 */
		@Override
		protected ConsumerTxnOperation commit() throws TxnException {
			consumer.accept( getTarget() );
			return this;
		}

		/**
		 * A no-op revert; the consumer has no rollback behavior.
		 *
		 * @return This operation
		 */
		@Override
		protected ConsumerTxnOperation revert() throws TxnException {
			return this;
		}

	}

}
