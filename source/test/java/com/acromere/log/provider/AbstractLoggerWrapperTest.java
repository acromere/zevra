package com.acromere.log.provider;

import com.acromere.log.LogData;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractLoggerWrapperTest {

	@Test
	void testDefaultMethods() {
		AbstractLoggerWrapper wrapper = new AbstractLoggerWrapper() {
			@Override
			public void log( LogData data ) {}

			@Override
			public void handleError( LogData data, RuntimeException error ) {}

			@Override
			public void flush() {}
		};

		assertThat( wrapper.getLoggerName() ).isNull();
		assertThat( wrapper.isLoggable( Level.INFO ) ).isFalse();
		assertThat( wrapper.isLoggable( Level.SEVERE ) ).isFalse();
		assertThat( wrapper.isLoggable( Level.FINEST ) ).isFalse();
	}

}
