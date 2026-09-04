package com.acromere.transaction;

import com.acromere.event.EventType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TxnEventTest {

	@Test
	void testEventConstants() {
		assertThat( TxnEvent.ANY.getParentEventType() ).isEqualTo( EventType.ROOT );
		assertThat( TxnEvent.ANY.getName() ).isEqualTo( "TXN" );

		assertThat( TxnEvent.COMMIT_BEGIN.getParentEventType() ).isEqualTo( TxnEvent.ANY );
		assertThat( TxnEvent.COMMIT_BEGIN.getName() ).isEqualTo( "COMMIT_BEGIN" );

		assertThat( TxnEvent.COMMIT_SUCCESS.getParentEventType() ).isEqualTo( TxnEvent.ANY );
		assertThat( TxnEvent.COMMIT_SUCCESS.getName() ).isEqualTo( "COMMIT_SUCCESS" );

		assertThat( TxnEvent.COMMIT_FAIL.getParentEventType() ).isEqualTo( TxnEvent.ANY );
		assertThat( TxnEvent.COMMIT_FAIL.getName() ).isEqualTo( "COMMIT_FAIL" );

		assertThat( TxnEvent.COMMIT_END.getParentEventType() ).isEqualTo( TxnEvent.ANY );
		assertThat( TxnEvent.COMMIT_END.getName() ).isEqualTo( "COMMIT_END" );
	}

	@Test
	void testConstructorAndGetters() {
		TxnEventTarget target = event -> {};
		TxnEvent event = new TxnEvent( target, TxnEvent.COMMIT_BEGIN );

		assertThat( event.getSource() ).isSameAs( target );
		assertThat( event.getEventType() ).isEqualTo( TxnEvent.COMMIT_BEGIN );
	}

	@Test
	void testCollapseUpDefault() {
		TxnEventTarget target = event -> {};
		TxnEvent event = new TxnEvent( target, TxnEvent.COMMIT_BEGIN );

		assertThat( event.collapseUp() ).isFalse();
	}

	@Test
	void testCollapseUpSubclass() {
		TxnEventTarget target = event -> {};
		CustomCollapseUpTxnEvent event = new CustomCollapseUpTxnEvent( target, TxnEvent.COMMIT_BEGIN );

		assertThat( event.collapseUp() ).isTrue();
	}

	private static class CustomCollapseUpTxnEvent extends TxnEvent {

		public CustomCollapseUpTxnEvent( TxnEventTarget source, EventType<? extends TxnEvent> type ) {
			super( source, type );
		}

		@Override
		public boolean collapseUp() {
			return true;
		}

	}

}
