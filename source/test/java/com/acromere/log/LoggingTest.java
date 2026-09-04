package com.acromere.log;

import com.acromere.log.java.JavaLoggingProvider;
import com.acromere.log.provider.AbstractLoggerWrapper;
import com.acromere.log.provider.AbstractLoggingProvider;
import com.acromere.log.provider.LoggerWrapper;
import com.acromere.log.provider.LoggingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingTest {

	@AfterEach
	void teardown() {
		Logging.setLoggingProvider( new JavaLoggingProvider() );
	}

	@Test
	void testDefaultLoggingProvider() {
		Logging.setLoggingProvider( new JavaLoggingProvider() );
		assertThat( Logging.getLoggingProvider() ).isInstanceOf( JavaLoggingProvider.class );
	}

	@Test
	void testSetLoggingProvider() {
		LoggingProvider customProvider = new AbstractLoggingProvider() {
			@Override
			public LoggerWrapper getLoggerWrapper( String name ) {
				return new AbstractLoggerWrapper() {
					@Override
					public String getLoggerName() {
						return name;
					}

					@Override
					public void log( LogData data ) {}

					@Override
					public void handleError( LogData data, RuntimeException error ) {}

					@Override
					public void flush() {}
				};
			}
		};

		Logging.setLoggingProvider( customProvider );
		assertThat( Logging.getLoggingProvider() ).isSameAs( customProvider );
	}

	@Test
	void testCreateLogger() {
		Logger logger = Logging.create( LoggingTest.class );
		assertThat( logger ).isNotNull();
		assertThat( logger.getName() ).isEqualTo( LoggingTest.class.getName() );
	}

}
