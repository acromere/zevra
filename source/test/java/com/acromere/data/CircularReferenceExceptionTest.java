package com.acromere.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CircularReferenceExceptionTest {

	@Test
	void testDefaultConstructor() {
		CircularReferenceException exception = new CircularReferenceException();
		assertThat( exception.getMessage() ).isNull();
		assertThat( exception.getCause() ).isNull();
	}

	@Test
	void testMessageConstructor() {
		CircularReferenceException exception = new CircularReferenceException( "test message" );
		assertThat( exception.getMessage() ).isEqualTo( "test message" );
		assertThat( exception.getCause() ).isNull();
	}

	@Test
	void testCauseConstructor() {
		Throwable cause = new RuntimeException( "cause" );
		CircularReferenceException exception = new CircularReferenceException( cause );
		assertThat( exception.getCause() ).isSameAs( cause );
		assertThat( exception.getMessage() ).contains( "cause" );
	}

	@Test
	void testMessageAndCauseConstructor() {
		Throwable cause = new RuntimeException( "cause" );
		CircularReferenceException exception = new CircularReferenceException( "test message", cause );
		assertThat( exception.getMessage() ).isEqualTo( "test message" );
		assertThat( exception.getCause() ).isSameAs( cause );
	}

}
