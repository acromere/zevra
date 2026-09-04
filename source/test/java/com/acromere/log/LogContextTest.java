package com.acromere.log;

import com.acromere.log.provider.AbstractLoggerWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LogContextTest {

	private TestWrapper wrapper;

	private TestLogger logger;

	@BeforeEach
	void setUp() {
		wrapper = new TestWrapper();
		logger = new TestLogger( wrapper );
	}

	@Test
	void testConstructorNullLevelThrowsNullPointerException() {
		assertThatThrownBy( () -> new TestContext( logger, null, false ) )
			.isInstanceOf( NullPointerException.class );
	}

	@Test
	void testConstructorWithDefaultTimestamp() {
		long beforeNanos = System.currentTimeMillis() * 1_000_000L;
		TestContext context = new TestContext( logger, Level.INFO, false );
		long afterNanos = (System.currentTimeMillis() + 100) * 1_000_000L;

		assertThat( context.getLevel() ).isEqualTo( Level.INFO );
		assertThat( context.wasForced() ).isFalse();
		assertThat( context.getTimestampNanos() ).isGreaterThanOrEqualTo( beforeNanos );
		assertThat( context.getTimestampNanos() ).isLessThanOrEqualTo( afterNanos );
		assertThat( context.getLoggerName() ).isEqualTo( "test-context" );
		assertThat( context.getMessage() ).isNull();
		assertThat( context.getMessageParameters() ).isEmpty();
		assertThat( context.getMetadata() ).isEmpty();
	}

	@Test
	void testConstructorWithInjectedTimestampAndForced() {
		TestContext context = new TestContext( logger, Level.WARNING, true, 999_888_777L );
		assertThat( context.getLevel() ).isEqualTo( Level.WARNING );
		assertThat( context.getTimestampNanos() ).isEqualTo( 999_888_777L );
		assertThat( context.wasForced() ).isTrue();
		assertThat( context.getMetadata().get( LogData.FORCED ) ).isEqualTo( Boolean.TRUE );
	}

	@Test
	void testIsEnabled() {
		wrapper.loggable = true;
		TestContext context1 = new TestContext( logger, Level.INFO, false );
		assertThat( context1.isEnabled() ).isTrue();

		wrapper.loggable = false;
		TestContext context2 = new TestContext( logger, Level.INFO, false );
		assertThat( context2.isEnabled() ).isFalse();

		// When forced, isEnabled is true even if logger isLoggable is false
		TestContext forcedContext = new TestContext( logger, Level.INFO, true );
		assertThat( forcedContext.isEnabled() ).isTrue();
	}

	@Test
	void testIsLiteral() {
		TestContext context = new TestContext( logger, Level.INFO, false );
		assertThat( context.isLiteral() ).isTrue();

		context.log( "Literal message" );
		assertThat( context.isLiteral() ).isTrue();

		TestContext contextWithArgs = new TestContext( logger, Level.INFO, false );
		contextWithArgs.log( "Message %s", "arg" );
		assertThat( contextWithArgs.isLiteral() ).isFalse();
	}

	@Test
	void testWithMetadata() {
		TestContext context = new TestContext( logger, Level.INFO, false );
		assertThatThrownBy( () -> context.with( null, "val" ) ).isInstanceOf( NullPointerException.class );
		assertThatThrownBy( () -> context.with( "customKey", null ) ).isInstanceOf( NullPointerException.class );

		context.with( "customKey", "customValue" );
		assertThat( context.getMetadata() ).containsEntry( "customKey", "customValue" );
	}

	@Test
	void testWithCause() {
		TestContext context = new TestContext( logger, Level.INFO, false );
		Throwable cause = new IllegalStateException( "failed state" );
		context.withCause( cause );
		assertThat( context.getMetadata().get( LogData.CAUSE ) ).isSameAs( cause );

		// null cause shouldn't fail
		context.withCause( null );
	}

	@Test
	void testLogWithoutArgs() {
		TestContext context = new TestContext( logger, Level.INFO, false );
		context.log();

		assertThat( wrapper.data ).isSameAs( context );
		assertThat( context.getMessage() ).isEqualTo( "" );
		assertThat( context.getMetadata().get( LogData.CLASS_NAME ) ).isNotNull();
		assertThat( context.getMetadata().get( LogData.METHOD_NAME ) ).isNotNull();
	}

	@Test
	void testLogWhenNotLoggableDoesNotCallWrite() {
		wrapper.loggable = false;
		TestContext context = new TestContext( logger, Level.INFO, false );
		context.log( "should not log" );
		assertThat( wrapper.data ).isNull();
	}

	@Test
	void testLogWithLazyEval() {
		TestContext context = new TestContext( logger, Level.INFO, false );
		context.log( "Result is %s and %d", LazyEval.of( () -> "lazy" ), LazyEval.of( () -> 123 ) );

		assertThat( context.getMessage() ).isEqualTo( "Result is lazy and 123" );
	}

	@Test
	void testLogVarargs() {
		TestContext context = new TestContext( logger, Level.INFO, false );
		context.logVarargs( "Values: %s, %s, %s", new Object[]{ "a", "b", "c" } );

		assertThat( context.getMessage() ).isEqualTo( "Values: a, b, c" );
		assertThat( context.getMessageParameters() ).containsExactly( "a", "b", "c" );
	}

	@Test
	void testLogVarargsWhenNotLoggable() {
		wrapper.loggable = false;
		TestContext context = new TestContext( logger, Level.INFO, false );
		context.logVarargs( "Values: %s", new Object[]{ "a" } );
		assertThat( wrapper.data ).isNull();
	}

	@Test
	void testLogOverloads1To10ArgsAndRest() {
		new TestContext( logger, Level.INFO, false ).log( "1: %s", "a" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1: a" );

		new TestContext( logger, Level.INFO, false ).log( "2: %s %s", "a", "b" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2: a b" );

		new TestContext( logger, Level.INFO, false ).log( "3: %s %s %s", "a", "b", "c" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3: a b c" );

		new TestContext( logger, Level.INFO, false ).log( "4: %s %s %s %s", "a", "b", "c", "d" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4: a b c d" );

		new TestContext( logger, Level.INFO, false ).log( "5: %s %s %s %s %s", "a", "b", "c", "d", "e" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5: a b c d e" );

		new TestContext( logger, Level.INFO, false ).log( "6: %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "6: a b c d e f" );

		new TestContext( logger, Level.INFO, false ).log( "7: %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "7: a b c d e f g" );

		new TestContext( logger, Level.INFO, false ).log( "8: %s %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g", "h" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "8: a b c d e f g h" );

		new TestContext( logger, Level.INFO, false ).log( "9: %s %s %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g", "h", "i" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "9: a b c d e f g h i" );

		new TestContext( logger, Level.INFO, false ).log( "10: %s %s %s %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "10: a b c d e f g h i j" );

		new TestContext( logger, Level.INFO, false ).log( "12: %s %s %s %s %s %s %s %s %s %s %s %s", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "12: a b c d e f g h i j k l" );
		assertThat( wrapper.data.getMessageParameters() ).hasSize( 12 );
	}

	@Test
	void testLogSinglePrimitiveOverloads() {
		new TestContext( logger, Level.INFO, false ).log( "char: %c", 'x' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "char: x" );

		new TestContext( logger, Level.INFO, false ).log( "byte: %d", (byte)10 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "byte: 10" );

		new TestContext( logger, Level.INFO, false ).log( "short: %d", (short)20 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "short: 20" );

		new TestContext( logger, Level.INFO, false ).log( "int: %d", 30 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "int: 30" );

		new TestContext( logger, Level.INFO, false ).log( "long: %d", 40L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "long: 40" );
	}

	@Test
	void testLogTwoArgumentsObjectAndPrimitiveOverloads() {
		new TestContext( logger, Level.INFO, false ).log( "%s %b", "str", true );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "str true" );

		new TestContext( logger, Level.INFO, false ).log( "%s %c", "str", 'A' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "str A" );

		new TestContext( logger, Level.INFO, false ).log( "%s %d", "str", (byte)1 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "str 1" );

		new TestContext( logger, Level.INFO, false ).log( "%s %d", "str", (short)2 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "str 2" );

		new TestContext( logger, Level.INFO, false ).log( "%s %d", "str", 3 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "str 3" );

		new TestContext( logger, Level.INFO, false ).log( "%s %d", "str", 4L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "str 4" );

		new TestContext( logger, Level.INFO, false ).log( "%s %.1f", "str", 5.5f );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "str 5.5" );

		new TestContext( logger, Level.INFO, false ).log( "%s %.1f", "str", 6.6d );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "str 6.6" );
	}

	@Test
	void testLogTwoArgumentsPrimitiveAndObjectOverloads() {
		new TestContext( logger, Level.INFO, false ).log( "%b %s", true, "str" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "true str" );

		new TestContext( logger, Level.INFO, false ).log( "%c %s", 'B', "str" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "B str" );

		new TestContext( logger, Level.INFO, false ).log( "%d %s", (byte)1, "str" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1 str" );

		new TestContext( logger, Level.INFO, false ).log( "%d %s", (short)2, "str" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2 str" );

		new TestContext( logger, Level.INFO, false ).log( "%d %s", 3, "str" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3 str" );

		new TestContext( logger, Level.INFO, false ).log( "%d %s", 4L, "str" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4 str" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %s", 5.5f, "str" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5.5 str" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %s", 6.6d, "str" );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "6.6 str" );
	}

	@Test
	void testLogTwoPrimitivesCombinations() {
		// boolean combinations
		new TestContext( logger, Level.INFO, false ).log( "%b %b", true, false );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "true false" );

		new TestContext( logger, Level.INFO, false ).log( "%b %c", true, 'Z' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "true Z" );

		new TestContext( logger, Level.INFO, false ).log( "%b %d", true, (byte)1 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "true 1" );

		new TestContext( logger, Level.INFO, false ).log( "%b %d", true, (short)2 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "true 2" );

		new TestContext( logger, Level.INFO, false ).log( "%b %d", true, 3 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "true 3" );

		new TestContext( logger, Level.INFO, false ).log( "%b %d", true, 4L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "true 4" );

		new TestContext( logger, Level.INFO, false ).log( "%b %.1f", true, 5.0f );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "true 5.0" );

		new TestContext( logger, Level.INFO, false ).log( "%b %.1f", true, 6.0d );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "true 6.0" );

		// char combinations
		new TestContext( logger, Level.INFO, false ).log( "%c %b", 'a', true );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "a true" );

		new TestContext( logger, Level.INFO, false ).log( "%c %c", 'a', 'b' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "a b" );

		new TestContext( logger, Level.INFO, false ).log( "%c %d", 'a', (byte)1 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "a 1" );

		new TestContext( logger, Level.INFO, false ).log( "%c %d", 'a', (short)2 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "a 2" );

		new TestContext( logger, Level.INFO, false ).log( "%c %d", 'a', 3 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "a 3" );

		new TestContext( logger, Level.INFO, false ).log( "%c %d", 'a', 4L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "a 4" );

		new TestContext( logger, Level.INFO, false ).log( "%c %.1f", 'a', 5.0f );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "a 5.0" );

		new TestContext( logger, Level.INFO, false ).log( "%c %.1f", 'a', 6.0d );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "a 6.0" );

		// byte combinations
		new TestContext( logger, Level.INFO, false ).log( "%d %b", (byte)1, true );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1 true" );

		new TestContext( logger, Level.INFO, false ).log( "%d %c", (byte)1, 'c' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1 c" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", (byte)1, (byte)2 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1 2" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", (byte)1, (short)3 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1 3" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", (byte)1, 4 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1 4" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", (byte)1, 5L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1 5" );

		new TestContext( logger, Level.INFO, false ).log( "%d %.1f", (byte)1, 6.0f );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1 6.0" );

		new TestContext( logger, Level.INFO, false ).log( "%d %.1f", (byte)1, 7.0d );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "1 7.0" );

		// short combinations
		new TestContext( logger, Level.INFO, false ).log( "%d %b", (short)2, true );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2 true" );

		new TestContext( logger, Level.INFO, false ).log( "%d %c", (short)2, 'c' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2 c" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", (short)2, (byte)1 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2 1" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", (short)2, (short)3 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2 3" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", (short)2, 4 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2 4" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", (short)2, 5L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2 5" );

		new TestContext( logger, Level.INFO, false ).log( "%d %.1f", (short)2, 6.0f );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2 6.0" );

		new TestContext( logger, Level.INFO, false ).log( "%d %.1f", (short)2, 7.0d );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "2 7.0" );

		// int combinations
		new TestContext( logger, Level.INFO, false ).log( "%d %b", 3, true );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3 true" );

		new TestContext( logger, Level.INFO, false ).log( "%d %c", 3, 'c' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3 c" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", 3, (byte)1 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3 1" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", 3, (short)2 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3 2" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", 3, 4 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3 4" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", 3, 5L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3 5" );

		new TestContext( logger, Level.INFO, false ).log( "%d %.1f", 3, 6.0f );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3 6.0" );

		new TestContext( logger, Level.INFO, false ).log( "%d %.1f", 3, 7.0d );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "3 7.0" );

		// long combinations
		new TestContext( logger, Level.INFO, false ).log( "%d %b", 4L, true );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4 true" );

		new TestContext( logger, Level.INFO, false ).log( "%d %c", 4L, 'c' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4 c" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", 4L, (byte)1 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4 1" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", 4L, (short)2 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4 2" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", 4L, 3 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4 3" );

		new TestContext( logger, Level.INFO, false ).log( "%d %d", 4L, 5L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4 5" );

		new TestContext( logger, Level.INFO, false ).log( "%d %.1f", 4L, 6.0f );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4 6.0" );

		new TestContext( logger, Level.INFO, false ).log( "%d %.1f", 4L, 7.0d );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "4 7.0" );

		// float combinations
		new TestContext( logger, Level.INFO, false ).log( "%.1f %b", 5.0f, true );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5.0 true" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %c", 5.0f, 'c' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5.0 c" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %d", 5.0f, (byte)1 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5.0 1" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %d", 5.0f, (short)2 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5.0 2" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %d", 5.0f, 3 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5.0 3" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %d", 5.0f, 4L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5.0 4" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %.1f", 5.0f, 6.0f );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5.0 6.0" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %.1f", 5.0f, 7.0d );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "5.0 7.0" );

		// double combinations
		new TestContext( logger, Level.INFO, false ).log( "%.1f %b", 8.0d, true );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "8.0 true" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %c", 8.0d, 'c' );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "8.0 c" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %d", 8.0d, (byte)1 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "8.0 1" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %d", 8.0d, (short)2 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "8.0 2" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %d", 8.0d, 3 );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "8.0 3" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %d", 8.0d, 4L );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "8.0 4" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %.1f", 8.0d, 5.0f );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "8.0 5.0" );

		new TestContext( logger, Level.INFO, false ).log( "%.1f %.1f", 8.0d, 9.0d );
		assertThat( wrapper.data.getMessage() ).isEqualTo( "8.0 9.0" );
	}

	private interface TestApi extends LogApi<TestApi> {}

	private static class TestLogger extends AbstractLogger<TestApi> {

		TestLogger( AbstractLoggerWrapper provider ) {
			super( provider );
		}

		@Override
		public TestApi at( Level level ) {
			return new TestContext( this, level, false );
		}

	}

	private static class TestNoOp extends LogApi.NoOp<TestApi> implements TestApi {}

	private static class TestContext extends LogContext<TestLogger, TestApi> implements TestApi {

		private final TestLogger logger;

		TestContext( TestLogger logger, Level level, boolean isForced ) {
			super( level, isForced );
			this.logger = logger;
		}

		TestContext( TestLogger logger, Level level, boolean isForced, long timestampNanos ) {
			super( level, isForced, timestampNanos );
			this.logger = logger;
		}

		@Override
		protected TestApi api() {
			return this;
		}

		@Override
		protected TestLogger getLogger() {
			return logger;
		}

		@Override
		protected TestApi noOp() {
			return new TestNoOp();
		}

	}

	private static class TestWrapper extends AbstractLoggerWrapper {

		private boolean loggable = true;

		private LogData data;

		@Override
		public String getLoggerName() {
			return "test-context";
		}

		@Override
		public boolean isLoggable( Level level ) {
			return loggable;
		}

		@Override
		public void log( LogData data ) {
			this.data = data;
		}

		@Override
		public void handleError( LogData data, RuntimeException error ) {}

		@Override
		public void flush() {}

	}

}
