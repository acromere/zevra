package com.acromere.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingExceptionTest {

	@Test
	void testMessageConstructor() {
		LoggingException exception = new LoggingException( "test error message" );
		assertThat( exception.getMessage() ).isEqualTo( "test error message" );
		assertThat( exception.getCause() ).isNull();
	}

	@Test
	void testMessageAndCauseConstructor() {
		Throwable cause = new IllegalArgumentException( "root cause" );
		LoggingException exception = new LoggingException( "test error message", cause );
		assertThat( exception.getMessage() ).isEqualTo( "test error message" );
		assertThat( exception.getCause() ).isSameAs( cause );
	}

}
