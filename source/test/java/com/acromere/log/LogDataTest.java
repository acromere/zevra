package com.acromere.log;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

public class LogDataTest {

	@Test
	void testConstants() {
		assertThat( LogData.CAUSE ).isEqualTo( "cause" );
		assertThat( LogData.FORCED ).isEqualTo( "forced" );
		assertThat( LogData.MODULE_NAME ).isEqualTo( "module-name" );
		assertThat( LogData.CLASS_NAME ).isEqualTo( "class-name" );
		assertThat( LogData.METHOD_NAME ).isEqualTo( "method-name" );
	}

	@Test
	void testLogDataImplementation() {
		LogData data = new LogData() {
			@Override
			public Level getLevel() {
				return Level.INFO;
			}

			@Override
			public long getTimestampNanos() {
				return 123456789L;
			}

			@Override
			public String getLoggerName() {
				return "testLogger";
			}

			@Override
			public boolean wasForced() {
				return false;
			}

			@Override
			public String getMessage() {
				return "test message";
			}

			@Override
			public Object[] getMessageParameters() {
				return new Object[]{ "arg1", 2 };
			}

			@Override
			public Map<Object, Object> getMetadata() {
				return Map.of( LogData.CLASS_NAME, "TestClass" );
			}
		};

		assertThat( data.getLevel() ).isEqualTo( Level.INFO );
		assertThat( data.getTimestampNanos() ).isEqualTo( 123456789L );
		assertThat( data.getLoggerName() ).isEqualTo( "testLogger" );
		assertThat( data.wasForced() ).isFalse();
		assertThat( data.getMessage() ).isEqualTo( "test message" );
		assertThat( data.getMessageParameters() ).containsExactly( "arg1", 2 );
		assertThat( data.getMetadata() ).containsEntry( LogData.CLASS_NAME, "TestClass" );
	}

}
