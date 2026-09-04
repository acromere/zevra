package com.acromere.util;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

class LogFlagTest {

	@Test
	void testConstants() {
		assertThat( LogFlag.LOG_APPEND ).isEqualTo( "--log-append" );
		assertThat( LogFlag.LOG_LEVEL ).isEqualTo( "--log-level" );
		assertThat( LogFlag.LOG_FILE ).isEqualTo( "--log-file" );
		assertThat( LogFlag.NONE ).isEqualTo( "none" );
		assertThat( LogFlag.ERROR ).isEqualTo( "error" );
		assertThat( LogFlag.WARN ).isEqualTo( "warn" );
		assertThat( LogFlag.INFO ).isEqualTo( "info" );
		assertThat( LogFlag.CONFIG ).isEqualTo( "config" );
		assertThat( LogFlag.DEBUG ).isEqualTo( "debug" );
		assertThat( LogFlag.TRACE ).isEqualTo( "trace" );
		assertThat( LogFlag.ALL ).isEqualTo( "all" );
	}

	@Test
	void testToLogLevel() {
		assertThat( LogFlag.toLogLevel( null ) ).isEqualTo( Level.OFF );
		assertThat( LogFlag.toLogLevel( "none" ) ).isEqualTo( Level.OFF );
		assertThat( LogFlag.toLogLevel( "NONE" ) ).isEqualTo( Level.OFF );
		assertThat( LogFlag.toLogLevel( "error" ) ).isEqualTo( Level.SEVERE );
		assertThat( LogFlag.toLogLevel( "ERROR" ) ).isEqualTo( Level.SEVERE );
		assertThat( LogFlag.toLogLevel( "warn" ) ).isEqualTo( Level.WARNING );
		assertThat( LogFlag.toLogLevel( "WARN" ) ).isEqualTo( Level.WARNING );
		assertThat( LogFlag.toLogLevel( "info" ) ).isEqualTo( Level.INFO );
		assertThat( LogFlag.toLogLevel( "INFO" ) ).isEqualTo( Level.INFO );
		assertThat( LogFlag.toLogLevel( "config" ) ).isEqualTo( Level.CONFIG );
		assertThat( LogFlag.toLogLevel( "CONFIG" ) ).isEqualTo( Level.CONFIG );
		assertThat( LogFlag.toLogLevel( "debug" ) ).isEqualTo( Level.FINE );
		assertThat( LogFlag.toLogLevel( "DEBUG" ) ).isEqualTo( Level.FINE );
		assertThat( LogFlag.toLogLevel( "trace" ) ).isEqualTo( Level.FINEST );
		assertThat( LogFlag.toLogLevel( "TRACE" ) ).isEqualTo( Level.FINEST );
		assertThat( LogFlag.toLogLevel( "all" ) ).isEqualTo( Level.ALL );
		assertThat( LogFlag.toLogLevel( "ALL" ) ).isEqualTo( Level.ALL );
		assertThat( LogFlag.toLogLevel( "unknown" ) ).isEqualTo( Level.OFF );
	}

}
