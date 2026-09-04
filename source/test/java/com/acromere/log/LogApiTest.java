package com.acromere.log;

import com.acromere.log.java.JavaLoggingProvider;
import com.acromere.log.provider.AbstractLoggerWrapper;
import com.acromere.log.provider.AbstractLoggingProvider;
import com.acromere.log.provider.LoggerWrapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

public class LogApiTest {

	private static TestWrapper wrapper;

	private static Logger log;

	static {
	}

	@BeforeAll
	static void setup() {
		Logging.setLoggingProvider( new TestProvider() );
		wrapper = (TestWrapper)Logging.getLoggingProvider().getLoggerWrapper( "" );
		log = new Logger( wrapper );
	}

	@AfterAll
	static void teardown() {
		Logging.setLoggingProvider( new JavaLoggingProvider() );
	}

	@Test
	void testAtInfo() {
		log.atInfo().log();
		assertThat( wrapper.getData().getLevel() ).isEqualTo( Level.INFO );
		assertThat( wrapper.getData().getMessage() ).isEqualTo( "" );
	}

	@Test
	void testLogWithMessage() {
		log.atInfo().log( "Hello World!" );
		assertThat( wrapper.getData().getLevel() ).isEqualTo( Level.INFO );
		assertThat( wrapper.getData().getMessage() ).isEqualTo( "Hello World!" );
	}

	@Test
	void testLogWithMessageAndParameter() {
		log.atInfo().log( "Hello %s!", "World" );
		assertThat( wrapper.getData().getLevel() ).isEqualTo( Level.INFO );
		assertThat( wrapper.getData().getMessage() ).isEqualTo( "Hello World!" );
	}

	@Test
	void testLogWithMessageAndLazyParameter() {
		log.atInfo().log( "Hello %s!", LazyEval.of( () -> "World" ) );
		assertThat( wrapper.getData().getLevel() ).isEqualTo( Level.INFO );
		assertThat( wrapper.getData().getMessage() ).isEqualTo( "Hello World!" );
	}

	@Test
	void testNoOp() {
		TestNoOp noOp = new TestNoOp();
		assertThat( noOp.isEnabled() ).isFalse();
		assertThat( noOp.isLiteral() ).isTrue();
		assertThat( noOp.with( "key", "val" ) ).isSameAs( noOp );
		assertThat( noOp.withCause( new RuntimeException() ) ).isSameAs( noOp );

		// Call all noOp methods to verify no exceptions are thrown
		noOp.log();
		noOp.log( "msg" );
		noOp.log( "msg %s", "arg" );
		noOp.log( "msg %s %s", "a", "b" );
		noOp.log( "msg %s %s %s", "a", "b", "c" );
		noOp.log( "msg %s %s %s %s", "a", "b", "c", "d" );
		noOp.log( "msg %s %s %s %s %s", "a", "b", "c", "d", "e" );
		noOp.log( "msg %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f" );
		noOp.log( "msg %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g" );
		noOp.log( "msg %s %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g", "h" );
		noOp.log( "msg %s %s %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g", "h", "i" );
		noOp.log( "msg %s %s %s %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" );
		noOp.log( "msg %s %s %s %s %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k" );
		noOp.log( "%c", 'a' );
		noOp.log( "%d", (byte)1 );
		noOp.log( "%d", (short)2 );
		noOp.log( "%d", 3 );
		noOp.log( "%d", 4L );
		noOp.log( "%s %b", "a", true );
		noOp.log( "%s %c", "a", 'b' );
		noOp.log( "%s %d", "a", (byte)1 );
		noOp.log( "%s %d", "a", (short)2 );
		noOp.log( "%s %d", "a", 3 );
		noOp.log( "%s %d", "a", 4L );
		noOp.log( "%s %f", "a", 5.0f );
		noOp.log( "%s %f", "a", 6.0d );
		noOp.log( "%b %s", true, "a" );
		noOp.log( "%c %s", 'b', "a" );
		noOp.log( "%d %s", (byte)1, "a" );
		noOp.log( "%d %s", (short)2, "a" );
		noOp.log( "%d %s", 3, "a" );
		noOp.log( "%d %s", 4L, "a" );
		noOp.log( "%f %s", 5.0f, "a" );
		noOp.log( "%f %s", 6.0d, "a" );
		noOp.log( "%b %b", true, false );
		noOp.log( "%b %c", true, 'a' );
		noOp.log( "%b %d", true, (byte)1 );
		noOp.log( "%b %d", true, (short)2 );
		noOp.log( "%b %d", true, 3 );
		noOp.log( "%b %d", true, 4L );
		noOp.log( "%b %f", true, 5.0f );
		noOp.log( "%b %f", true, 6.0d );
		noOp.log( "%c %b", 'a', true );
		noOp.log( "%c %c", 'a', 'b' );
		noOp.log( "%c %d", 'a', (byte)1 );
		noOp.log( "%c %d", 'a', (short)2 );
		noOp.log( "%c %d", 'a', 3 );
		noOp.log( "%c %d", 'a', 4L );
		noOp.log( "%c %f", 'a', 5.0f );
		noOp.log( "%c %f", 'a', 6.0d );
		noOp.log( "%d %b", (byte)1, true );
		noOp.log( "%d %c", (byte)1, 'a' );
		noOp.log( "%d %d", (byte)1, (byte)2 );
		noOp.log( "%d %d", (byte)1, (short)2 );
		noOp.log( "%d %d", (byte)1, 3 );
		noOp.log( "%d %d", (byte)1, 4L );
		noOp.log( "%d %f", (byte)1, 5.0f );
		noOp.log( "%d %f", (byte)1, 6.0d );
		noOp.log( "%d %b", (short)2, true );
		noOp.log( "%d %c", (short)2, 'a' );
		noOp.log( "%d %d", (short)2, (byte)1 );
		noOp.log( "%d %d", (short)2, (short)2 );
		noOp.log( "%d %d", (short)2, 3 );
		noOp.log( "%d %d", (short)2, 4L );
		noOp.log( "%d %f", (short)2, 5.0f );
		noOp.log( "%d %f", (short)2, 6.0d );
		noOp.log( "%d %b", 3, true );
		noOp.log( "%d %c", 3, 'a' );
		noOp.log( "%d %d", 3, (byte)1 );
		noOp.log( "%d %d", 3, (short)2 );
		noOp.log( "%d %d", 3, 3 );
		noOp.log( "%d %d", 3, 4L );
		noOp.log( "%d %f", 3, 5.0f );
		noOp.log( "%d %f", 3, 6.0d );
		noOp.log( "%d %b", 4L, true );
		noOp.log( "%d %c", 4L, 'a' );
		noOp.log( "%d %d", 4L, (byte)1 );
		noOp.log( "%d %d", 4L, (short)2 );
		noOp.log( "%d %d", 4L, 3 );
		noOp.log( "%d %d", 4L, 4L );
		noOp.log( "%d %f", 4L, 5.0f );
		noOp.log( "%d %f", 4L, 6.0d );
		noOp.log( "%f %b", 5.0f, true );
		noOp.log( "%f %c", 5.0f, 'a' );
		noOp.log( "%f %d", 5.0f, (byte)1 );
		noOp.log( "%f %d", 5.0f, (short)2 );
		noOp.log( "%f %d", 5.0f, 3 );
		noOp.log( "%f %d", 5.0f, 4L );
		noOp.log( "%f %f", 5.0f, 5.0f );
		noOp.log( "%f %f", 5.0f, 6.0d );
		noOp.log( "%f %b", 6.0d, true );
		noOp.log( "%f %c", 6.0d, 'a' );
		noOp.log( "%f %d", 6.0d, (byte)1 );
		noOp.log( "%f %d", 6.0d, (short)2 );
		noOp.log( "%f %d", 6.0d, 3 );
		noOp.log( "%f %d", 6.0d, 4L );
		noOp.log( "%f %f", 6.0d, 5.0f );
		noOp.log( "%f %f", 6.0d, 6.0d );
		noOp.logVarargs( "varargs %s", new Object[]{ "test" } );
	}

	private interface TestLogApi extends LogApi<TestLogApi> {}

	private static class TestNoOp extends LogApi.NoOp<TestLogApi> implements TestLogApi {}

	private static class TestProvider extends AbstractLoggingProvider {

		private final TestWrapper wrapper = new TestWrapper();

		@Override
		public LoggerWrapper getLoggerWrapper( String name ) {
			return wrapper;
		}

	}

	private static class TestWrapper extends AbstractLoggerWrapper {

		private LogData data;

		private RuntimeException exception;

		@Override
		public String getLoggerName() {
			return "test";
		}

		@Override
		public boolean isLoggable( Level level ) {
			return true;
		}

		@Override
		public void log( LogData data ) {
			this.data = data;
		}

		@Override
		public void handleError( LogData data, RuntimeException exception ) {
			this.data = data;
			this.exception = exception;
		}

		@Override
		public void flush() {}

		public LogData getData() {
			return data;
		}

		public RuntimeException getException() {
			return exception;
		}

	}

}
