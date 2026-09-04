package com.acromere.util;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramFormatterTest {

	@Test
	void testFormatBasicRecord() {
		ProgramFormatter formatter = new ProgramFormatter();
		LogRecord record = new LogRecord( Level.INFO, "Test message" );
		record.setLoggerName( "test.logger" );

		String formatted = formatter.format( record );

		assertThat( formatted ).contains( "test.logger" );
		assertThat( formatted ).contains( "[I]" );
		assertThat( formatted ).contains( "Test message" );
		assertThat( formatted ).endsWith( System.lineSeparator() );
	}

	@Test
	void testFormatWithSourceClassAndMethod() {
		ProgramFormatter formatter = new ProgramFormatter();
		LogRecord record = new LogRecord( Level.WARNING, "Warning message" );
		record.setLoggerName( "my.logger" );
		record.setSourceClassName( "com.acromere.util.MyClass" );
		record.setSourceMethodName( "doSomething" );

		String formatted = formatter.format( record );

		assertThat( formatted ).contains( "MyClass.doSomething" );
		assertThat( formatted ).contains( "[W]" );
		assertThat( formatted ).contains( "Warning message" );
	}

	@Test
	void testFormatWithSourceClassWithoutMethod() {
		ProgramFormatter formatter = new ProgramFormatter();
		LogRecord record = new LogRecord( Level.SEVERE, "Severe error" );
		record.setLoggerName( "my.logger" );
		record.setSourceClassName( "com.acromere.util.MyClass" );

		String formatted = formatter.format( record );

		assertThat( formatted ).contains( "MyClass" );
		assertThat( formatted ).contains( "[E]" );
		assertThat( formatted ).contains( "Severe error" );
	}

	@Test
	void testFormatLevels() {
		ProgramFormatter formatter = new ProgramFormatter();

		LogRecord configRecord = new LogRecord( Level.CONFIG, "Config message" );
		assertThat( formatter.format( configRecord ) ).contains( "[C]" );

		LogRecord fineRecord = new LogRecord( Level.FINE, "Fine message" );
		assertThat( formatter.format( fineRecord ) ).contains( "[D]" );

		LogRecord finerRecord = new LogRecord( Level.FINER, "Finer message" );
		assertThat( formatter.format( finerRecord ) ).contains( "[T]" );

		LogRecord finestRecord = new LogRecord( Level.FINEST, "Finest message" );
		assertThat( formatter.format( finestRecord ) ).contains( "[F]" );

		LogRecord allRecord = new LogRecord( Level.ALL, "All message" );
		assertThat( formatter.format( allRecord ) ).contains( "[A]" );

		LogRecord offRecord = new LogRecord( Level.OFF, "Off message" );
		assertThat( formatter.format( offRecord ) ).contains( "[O]" );
	}

	@Test
	void testFormatWithThrowable() {
		ProgramFormatter formatter = new ProgramFormatter();
		LogRecord record = new LogRecord( Level.SEVERE, "Error with exception" );
		record.setThrown( new RuntimeException( "Test exception occurred" ) );

		String formatted = formatter.format( record );

		assertThat( formatted ).contains( "Error with exception" );
		assertThat( formatted ).contains( "RuntimeException: Test exception occurred" );
	}

	@Test
	void testFormatWithParameters() {
		ProgramFormatter formatter = new ProgramFormatter();
		LogRecord record = new LogRecord( Level.INFO, "Count is {0} and status is {1}" );
		record.setParameters( new Object[]{ 42, "OK" } );

		String formatted = formatter.format( record );

		assertThat( formatted ).contains( "Count is 42 and status is OK" );
	}

}
