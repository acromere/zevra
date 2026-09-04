package com.acromere.transaction;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TxnOperationTest {

	@Test
	void testConstructorAndInitialState() {
		TxnEventTarget target = event -> {};
		SimpleTestOperation operation = new SimpleTestOperation( target );

		assertThat( operation.getTarget() ).isSameAs( target );
		assertThat( operation.getResult() ).isNotNull();
		assertThat( operation.getResult().getOperation() ).isSameAs( operation );
		assertThat( operation.getStatus() ).isEqualTo( TxnOperation.Status.WAITING );
	}

	@Test
	void testCallCommitSuccess() throws TxnException {
		TxnEventTarget target = event -> {};
		AtomicReference<TxnOperation.Status> statusDuringCommit = new AtomicReference<>();

		TxnOperation operation = new TxnOperation( target ) {
			@Override
			protected TxnOperation commit() {
				statusDuringCommit.set( getStatus() );
				return this;
			}

			@Override
			protected TxnOperation revert() {
				return this;
			}
		};

		TxnOperationResult result = operation.callCommit();

		assertThat( statusDuringCommit.get() ).isEqualTo( TxnOperation.Status.COMMITTING );
		assertThat( operation.getStatus() ).isEqualTo( TxnOperation.Status.COMMITTED );
		assertThat( result ).isSameAs( operation.getResult() );
	}

	@Test
	void testCallCommitThrowsTxnException() {
		TxnEventTarget target = event -> {};
		TxnException expectedException = new TxnException( "Commit failed" );

		TxnOperation operation = new TxnOperation( target ) {
			@Override
			protected TxnOperation commit() throws TxnException {
				throw expectedException;
			}

			@Override
			protected TxnOperation revert() {
				return this;
			}
		};

		assertThatThrownBy( operation::callCommit )
			.isSameAs( expectedException );
		assertThat( operation.getStatus() ).isEqualTo( TxnOperation.Status.FAILED );
	}

	@Test
	void testCallRevertSuccess() throws TxnException {
		TxnEventTarget target = event -> {};
		AtomicReference<TxnOperation.Status> statusDuringRevert = new AtomicReference<>();

		TxnOperation operation = new TxnOperation( target ) {
			@Override
			protected TxnOperation commit() {
				return this;
			}

			@Override
			protected TxnOperation revert() {
				statusDuringRevert.set( getStatus() );
				return this;
			}
		};

		operation.callCommit();
		operation.callRevert();

		assertThat( statusDuringRevert.get() ).isEqualTo( TxnOperation.Status.REVERTING );
		assertThat( operation.getStatus() ).isEqualTo( TxnOperation.Status.REVERTED );
	}

	@Test
	void testCallRevertThrowsTxnException() {
		TxnEventTarget target = event -> {};
		TxnException expectedException = new TxnException( "Revert failed" );

		TxnOperation operation = new TxnOperation( target ) {
			@Override
			protected TxnOperation commit() {
				return this;
			}

			@Override
			protected TxnOperation revert() throws TxnException {
				throw expectedException;
			}
		};

		assertThatThrownBy( operation::callRevert )
			.isSameAs( expectedException );
		assertThat( operation.getStatus() ).isEqualTo( TxnOperation.Status.FAILED );
	}

	@Test
	void testToString() {
		TxnEventTarget target = event -> {};
		SimpleTestOperation operation = new SimpleTestOperation( target );

		String expected = "SimpleTestOperation[" + System.identityHashCode( operation ) + "]";
		assertThat( operation.toString() ).isEqualTo( expected );
	}

	@Test
	void testStatusEnum() {
		assertThat( TxnOperation.Status.values() ).containsExactly(
			TxnOperation.Status.WAITING,
			TxnOperation.Status.COMMITTING,
			TxnOperation.Status.COMMITTED,
			TxnOperation.Status.REVERTING,
			TxnOperation.Status.REVERTED,
			TxnOperation.Status.FAILED
		);

		assertThat( TxnOperation.Status.valueOf( "WAITING" ) ).isEqualTo( TxnOperation.Status.WAITING );
	}

	private static class SimpleTestOperation extends TxnOperation {

		public SimpleTestOperation( TxnEventTarget target ) {
			super( target );
		}

		@Override
		protected TxnOperation commit() {
			return this;
		}

		@Override
		protected TxnOperation revert() {
			return this;
		}

	}

}
