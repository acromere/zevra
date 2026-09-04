package com.acromere.log.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingProviderTest {

	@Test
	void testGetCurrentTimeNanos() {
		long beforeNanos = System.currentTimeMillis() * 1_000_000L;
		long timeNanos = LoggingProvider.getCurrentTimeNanos();
		long afterNanos = (System.currentTimeMillis() + 100) * 1_000_000L;

		assertThat( timeNanos ).isGreaterThanOrEqualTo( beforeNanos );
		assertThat( timeNanos ).isLessThanOrEqualTo( afterNanos );
	}

	@Test
	void testAbstractLoggingProvider() {
		AbstractLoggingProvider provider = new AbstractLoggingProvider() {
			@Override
			public LoggerWrapper getLoggerWrapper( String name ) {
				return null;
			}
		};

		assertThat( provider ).isInstanceOf( LoggingProvider.class );
	}

}
