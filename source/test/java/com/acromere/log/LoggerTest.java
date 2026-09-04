package com.acromere.log;

import com.acromere.log.provider.AbstractLoggerWrapper;
import com.acromere.log.provider.LoggerWrapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LoggerTest {

	@Test
	void testConstructorNullProvider() {
		assertThatThrownBy( () -> new Logger( null ) ).isInstanceOf( NullPointerException.class );
	}

	@Test
	void testGetNameAndGetProvider() {
		TestWrapper wrapper = new TestWrapper();
		wrapper.name = "custom.logger";
		Logger logger = new Logger( wrapper );

		assertThat( logger.getName() ).isEqualTo( "custom.logger" );
		assertThat( logger.getProvider() ).isSameAs( wrapper );
	}

	@Test
	void testFlush() {
		AtomicBoolean flushed = new AtomicBoolean( false );
		LoggerWrapper wrapper = new AbstractLoggerWrapper() {
			@Override
			public void log( LogData data ) {}

			@Override
			public void handleError( LogData data, RuntimeException error ) {}

			@Override
			public void flush() {
				flushed.set( true );
			}
		};

		Logger logger = new Logger( wrapper );
		logger.flush();
		assertThat( flushed.get() ).isTrue();
	}

	@Test
	void testAtWhenNotLoggableReturnsNoOp() {
		TestWrapper wrapper = new TestWrapper();
		wrapper.loggable = false;
		Logger logger = new Logger( wrapper );

		Logger.Api api = logger.at( Level.INFO );
		assertThat( api ).isSameAs( Logger.NO_OP );
		assertThat( api.isEnabled() ).isFalse();
	}

	@Test
	void testAtWhenLoggableReturnsContext() {
		TestWrapper wrapper = new TestWrapper();
		wrapper.loggable = true;
		Logger logger = new Logger( wrapper );

		Logger.Api api = logger.at( Level.INFO );
		assertThat( api ).isNotSameAs( Logger.NO_OP );
		assertThat( api.isEnabled() ).isTrue();
		assertThat( api ).isInstanceOf( Logger.Context.class );
	}

	@Test
	void testConvenienceLevelMethods() {
		TestWrapper wrapper = new TestWrapper();
		wrapper.loggable = true;
		Logger logger = new Logger( wrapper );

		logger.atSevere().log( "severe" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.SEVERE );

		logger.atError().log( "error" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.SEVERE );

		Throwable errorCause = new RuntimeException( "error cause" );
		logger.atError( errorCause ).log( "error with cause" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.SEVERE );
		assertThat( wrapper.data.getMetadata().get( LogData.CAUSE ) ).isSameAs( errorCause );

		logger.atWarning().log( "warning" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.WARNING );

		logger.atWarn().log( "warn" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.WARNING );

		Throwable warnCause = new RuntimeException( "warn cause" );
		logger.atWarn( warnCause ).log( "warn with cause" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.WARNING );
		assertThat( wrapper.data.getMetadata().get( LogData.CAUSE ) ).isSameAs( warnCause );

		logger.atInfo().log( "info" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.INFO );

		logger.atConfig().log( "config" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.CONFIG );

		logger.atFine().log( "fine" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.FINE );

		logger.atDebug().log( "debug" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.FINE );

		logger.atFiner().log( "finer" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.FINER );

		logger.atTrace().log( "trace" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.FINER );

		logger.atFinest().log( "finest" );
		assertThat( wrapper.data.getLevel() ).isEqualTo( Level.FINEST );
	}

	@Test
	void testWriteNullDataThrowsNullPointerException() {
		TestWrapper wrapper = new TestWrapper();
		Logger logger = new Logger( wrapper );
		assertThatThrownBy( () -> logger.write( null ) ).isInstanceOf( NullPointerException.class );
	}

	@Test
	void testWriteProviderThrowsExceptionCallsHandleError() {
		TestWrapper wrapper = new TestWrapper();
		wrapper.throwOnLog = new RuntimeException( "provider log failed" );
		Logger logger = new Logger( wrapper );

		logger.at( Level.INFO ).log( "test message" );
		assertThat( wrapper.handledError ).isSameAs( wrapper.throwOnLog );
	}

	@Test
	void testWriteHandleErrorThrowsLoggingExceptionRethrown() {
		TestWrapper wrapper = new TestWrapper();
		wrapper.throwOnLog = new RuntimeException( "log failed" );
		wrapper.throwOnHandleError = new LoggingException( "rethrow allowed" );
		Logger logger = new Logger( wrapper );

		assertThatThrownBy( () -> logger.at( Level.INFO ).log( "test" ) )
			.isInstanceOf( LoggingException.class )
			.hasMessage( "rethrow allowed" );
	}

	@Test
	void testWriteHandleErrorThrowsRuntimeExceptionSwallowedWithStderr() {
		PrintStream originalErr = System.err;
		ByteArrayOutputStream errOut = new ByteArrayOutputStream();
		try {
			System.setErr( new PrintStream( errOut ) );

			TestWrapper wrapper = new TestWrapper();
			wrapper.throwOnLog = new RuntimeException( "log failed" );
			wrapper.throwOnHandleError = new IllegalStateException( "handleError failed" );
			Logger logger = new Logger( wrapper );

			logger.at( Level.INFO ).log( "test" );
			assertThat( errOut.toString() ).contains( "logging error: handleError failed" );
		} finally {
			System.setErr( originalErr );
		}
	}

	@Test
	void testContextInternalMethods() {
		TestWrapper wrapper = new TestWrapper();
		wrapper.loggable = true;
		Logger logger = new Logger( wrapper );

		Logger.Api api = logger.at( Level.INFO );
		assertThat( api ).isInstanceOf( Logger.Context.class );
		Logger.Context context = (Logger.Context)api;

		assertThat( context.getLogger() ).isSameAs( logger );
		assertThat( context.api() ).isSameAs( context );
		assertThat( context.noOp() ).isSameAs( Logger.NO_OP );
	}

	private static class TestWrapper extends AbstractLoggerWrapper {

		private String name = "test";

		private boolean loggable = true;

		private LogData data;

		private RuntimeException throwOnLog;

		private RuntimeException throwOnHandleError;

		private RuntimeException handledError;

		@Override
		public String getLoggerName() {
			return name;
		}

		@Override
		public boolean isLoggable( Level level ) {
			return loggable;
		}

		@Override
		public void log( LogData data ) {
			if( throwOnLog != null ) throw throwOnLog;
			this.data = data;
		}

		@Override
		public void handleError( LogData data, RuntimeException error ) {
			this.handledError = error;
			if( throwOnHandleError != null ) throw throwOnHandleError;
		}

		@Override
		public void flush() {}

	}

}
