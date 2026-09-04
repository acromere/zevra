package com.acromere.log.java;

import com.acromere.log.LogData;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaLoggingLoggerWrapperTest {

	@Test
	void testGetLoggerName() {
		Logger julLogger = Logger.getLogger( "test.wrapper.name" );
		JavaLoggingLoggerWrapper wrapper = new JavaLoggingLoggerWrapper( julLogger );
		assertThat( wrapper.getLoggerName() ).isEqualTo( "test.wrapper.name" );
	}

	@Test
	void testIsLoggable() {
		Logger julLogger = Logger.getLogger( "test.wrapper.loggable" );
		julLogger.setLevel( Level.WARNING );
		JavaLoggingLoggerWrapper wrapper = new JavaLoggingLoggerWrapper( julLogger );

		assertThat( wrapper.isLoggable( Level.SEVERE ) ).isTrue();
		assertThat( wrapper.isLoggable( Level.WARNING ) ).isTrue();
		assertThat( wrapper.isLoggable( Level.INFO ) ).isFalse();
	}

	@Test
	void testLog() {
		Logger julLogger = Logger.getLogger( "test.wrapper.log" );
		julLogger.setLevel( Level.ALL );
		julLogger.setUseParentHandlers( false );

		List<LogRecord> published = new ArrayList<>();
		julLogger.addHandler( new Handler() {
			@Override
			public void publish( LogRecord record ) {
				published.add( record );
			}

			@Override
			public void flush() {}

			@Override
			public void close() {}
		} );

		JavaLoggingLoggerWrapper wrapper = new JavaLoggingLoggerWrapper( julLogger );

		Throwable cause = new RuntimeException( "test cause" );
		long timestampNanos = 1_700_000_000_123_456_789L;
		LogData data = new LogData() {
			@Override
			public Level getLevel() {
				return Level.INFO;
			}

			@Override
			public long getTimestampNanos() {
				return timestampNanos;
			}

			@Override
			public String getLoggerName() {
				return "test.wrapper.log";
			}

			@Override
			public boolean wasForced() {
				return false;
			}

			@Override
			public String getMessage() {
				return "formatted message";
			}

			@Override
			public Object[] getMessageParameters() {
				return new Object[]{ "param1" };
			}

			@Override
			public Map<Object, Object> getMetadata() {
				return Map.of(
					LogData.CLASS_NAME, "MyClass",
					LogData.METHOD_NAME, "myMethod",
					LogData.CAUSE, cause
				);
			}
		};

		wrapper.log( data );

		assertThat( published ).hasSize( 1 );
		LogRecord record = published.get( 0 );
		assertThat( record.getLevel() ).isEqualTo( Level.INFO );
		assertThat( record.getMessage() ).isEqualTo( "formatted message" );
		assertThat( record.getParameters() ).containsExactly( "param1" );
		assertThat( record.getSourceClassName() ).isEqualTo( "MyClass" );
		assertThat( record.getSourceMethodName() ).isEqualTo( "myMethod" );
		assertThat( record.getThrown() ).isSameAs( cause );
		assertThat( record.getInstant() ).isEqualTo( Instant.ofEpochSecond( timestampNanos / 1_000_000_000L, timestampNanos % 1_000_000_000L ) );
	}

	@Test
	void testHandleError() {
		Logger julLogger = Logger.getLogger( "test.wrapper.error" );
		JavaLoggingLoggerWrapper wrapper = new JavaLoggingLoggerWrapper( julLogger );
		// Should execute cleanly without throwing
		wrapper.handleError( null, new RuntimeException( "test error" ) );
	}

	@Test
	void testFlush() {
		Logger julLogger = Logger.getLogger( "test.wrapper.flush" );
		AtomicBoolean flushed = new AtomicBoolean( false );
		julLogger.addHandler( new Handler() {
			@Override
			public void publish( LogRecord record ) {}

			@Override
			public void flush() {
				flushed.set( true );
			}

			@Override
			public void close() {}
		} );

		JavaLoggingLoggerWrapper wrapper = new JavaLoggingLoggerWrapper( julLogger );
		wrapper.flush();
		assertThat( flushed.get() ).isTrue();
	}

}
