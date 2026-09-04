package com.acromere.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TxnExceptionTest {

	@Test
	void testDefaultConstructor() {
		TxnException exception = new TxnException();
		assertThat( exception.getMessage() ).isNull();
		assertThat( exception.getCause() ).isNull();
	}

	@Test
	void testMessageConstructor() {
		TxnException exception = new TxnException( "test message" );
		assertThat( exception.getMessage() ).isEqualTo( "test message" );
		assertThat( exception.getCause() ).isNull();
	}

	@Test
	void testCauseConstructor() {
		Throwable cause = new RuntimeException( "cause" );
		TxnException exception = new TxnException( cause );
		assertThat( exception.getCause() ).isSameAs( cause );
		assertThat( exception.getMessage() ).contains( "cause" );
	}

	@Test
	void testMessageAndCauseConstructor() {
		Throwable cause = new RuntimeException( "cause" );
		TxnException exception = new TxnException( "test message", cause );
		assertThat( exception.getMessage() ).isEqualTo( "test message" );
		assertThat( exception.getCause() ).isSameAs( cause );
	}

}
