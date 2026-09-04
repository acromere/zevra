package com.acromere.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TxnEventWrapperTest {

	@Test
	void testConstructorAndGetters() {
		TxnEventTarget target = event -> {};
		TxnEvent event = new TxnEvent( target, TxnEvent.COMMIT_BEGIN );

		TxnEventWrapper wrapper = new TxnEventWrapper( target, event );

		assertThat( wrapper.target() ).isSameAs( target );
		assertThat( wrapper.event() ).isSameAs( event );
	}

	@Test
	void testConstructorNullTargetThrowsException() {
		TxnEventTarget target = event -> {};
		TxnEvent event = new TxnEvent( target, TxnEvent.COMMIT_BEGIN );

		assertThatThrownBy( () -> new TxnEventWrapper( null, event ) )
			.isInstanceOf( NullPointerException.class )
			.hasMessage( "Target cannot be null" );
	}

	@Test
	void testConstructorNullEventThrowsException() {
		TxnEventTarget target = event -> {};

		assertThatThrownBy( () -> new TxnEventWrapper( target, null ) )
			.isInstanceOf( NullPointerException.class )
			.hasMessage( "Event cannot be null" );
	}

}
