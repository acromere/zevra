package com.acromere.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TxnOperationResultTest {

	@Test
	void testConstructorAndGetOperation() {
		TxnEventTarget target = event -> {};
		TestOperation operation = new TestOperation( target );
		TxnOperationResult result = new TxnOperationResult( operation );

		assertThat( result.getOperation() ).isSameAs( operation );
		assertThat( result.getEvents() ).isNotNull().isEmpty();
	}

	@Test
	void testAddEvent() {
		TxnEventTarget target = event -> {};
		TestOperation operation = new TestOperation( target );
		TxnOperationResult result = new TxnOperationResult( operation );

		TxnEvent event = new TxnEvent( target, TxnEvent.COMMIT_BEGIN );
		result.addEvent( target, event );

		assertThat( result.getEvents() ).hasSize( 1 );
		assertThat( result.getEvents().get( 0 ).target() ).isSameAs( target );
		assertThat( result.getEvents().get( 0 ).event() ).isSameAs( event );
	}

	@Test
	void testAddEventsFrom() {
		TxnEventTarget target1 = event -> {};
		TxnEventTarget target2 = event -> {};

		TestOperation operation1 = new TestOperation( target1 );
		TestOperation operation2 = new TestOperation( target2 );

		TxnEvent event1 = new TxnEvent( target1, TxnEvent.COMMIT_BEGIN );
		TxnEvent event2 = new TxnEvent( target2, TxnEvent.COMMIT_SUCCESS );

		operation2.getResult().addEvent( target2, event2 );

		TxnOperationResult result1 = operation1.getResult();
		result1.addEvent( target1, event1 );
		result1.addEventsFrom( operation2 );

		assertThat( result1.getEvents() ).hasSize( 2 );
		assertThat( result1.getEvents().get( 0 ).target() ).isSameAs( target1 );
		assertThat( result1.getEvents().get( 0 ).event() ).isSameAs( event1 );
		assertThat( result1.getEvents().get( 1 ).target() ).isSameAs( target2 );
		assertThat( result1.getEvents().get( 1 ).event() ).isSameAs( event2 );
	}

	private static class TestOperation extends TxnOperation {

		public TestOperation( TxnEventTarget target ) {
			super( target );
		}

		@Override
		protected TxnOperation commit() throws TxnException {
			return this;
		}

		@Override
		protected TxnOperation revert() throws TxnException {
			return this;
		}

	}

}
