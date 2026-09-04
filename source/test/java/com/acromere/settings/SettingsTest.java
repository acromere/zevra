package com.acromere.settings;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsTest {

	@Test
	void testPrint() {
		Settings settings = new MapSettings();
		settings.set( "key1", "value1" );
		settings.set( "key2", "value2" );

		PrintStream originalOut = System.out;
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		try {
			System.setOut( new PrintStream( outContent, true, StandardCharsets.UTF_8 ) );
			Settings.print( settings );
		} finally {
			System.setOut( originalOut );
		}

		String output = outContent.toString( StandardCharsets.UTF_8 );
		assertThat( output ).contains( "settings( / ) {" );
		assertThat( output ).contains( "key1 = value1" );
		assertThat( output ).contains( "key2 = value2" );
		assertThat( output ).contains( "}" );
	}

	@Test
	void testDefaultGetOverloads() {
		Settings settings = new MapSettings();
		settings.set( "strKey", "hello" );
		settings.set( "intKey", 42 );

		// get(key)
		assertThat( settings.get( "strKey" ) ).isEqualTo( "hello" );
		assertThat( settings.get( "missing" ) ).isNull();

		// get(key, defaultValue)
		assertThat( settings.get( "strKey", "default" ) ).isEqualTo( "hello" );
		assertThat( settings.get( "missing", "default" ) ).isEqualTo( "default" );

		// get(key, Object defaultValue)
		assertThat( settings.get( "strKey", 123 ) ).isEqualTo( "hello" );
		assertThat( settings.get( "missing", 123 ) ).isEqualTo( "123" );

		// get(key, Class)
		assertThat( settings.get( "intKey", Integer.class ) ).isEqualTo( 42 );
		assertThat( settings.get( "missing", Integer.class ) ).isNull();

		// get(key, Class, defaultValue)
		assertThat( settings.get( "intKey", Integer.class, 99 ) ).isEqualTo( 42 );
		assertThat( settings.get( "missing", Integer.class, 99 ) ).isEqualTo( 99 );
	}

	@Test
	void testBindWithDefaultValue() {
		Settings settings = new MapSettings();
		SettingsEventWatcher watcher = new SettingsEventWatcher();

		settings.bind( "missingKey", "defaultValue", watcher );

		assertThat( watcher.getEvents() ).hasSize( 1 );
		SettingsEvent initialEvent = watcher.getEvents().getFirst();
		assertThat( initialEvent.getNewValue() ).isEqualTo( "defaultValue" );
		assertThat( initialEvent.getKey() ).isEqualTo( "missingKey" );
		assertThat( initialEvent.getPath() ).isEqualTo( "/" );
		assertThat( initialEvent.getEventType() ).isEqualTo( SettingsEvent.CHANGED );

		// Mutate setting, handler receives event
		settings.set( "missingKey", "newValue" );
		assertThat( watcher.getEvents() ).hasSize( 2 );
		SettingsEvent updatedEvent = watcher.getEvents().get( 1 );
		assertThat( updatedEvent.getNewValue() ).isEqualTo( "newValue" );
	}

	@Test
	void testBindWithTypeAndDefaultValue() {
		Settings settings = new MapSettings();
		SettingsEventWatcher watcher = new SettingsEventWatcher();

		settings.bind( "intKey", Integer.class, 50, watcher );

		assertThat( watcher.getEvents() ).hasSize( 1 );
		SettingsEvent initialEvent = watcher.getEvents().getFirst();
		assertThat( initialEvent.getNewValue() ).isEqualTo( 50 );
		assertThat( initialEvent.getKey() ).isEqualTo( "intKey" );

		settings.set( "intKey", 100 );
		assertThat( watcher.getEvents() ).hasSize( 2 );
		assertThat( watcher.getEvents().get( 1 ).getNewValue() ).isEqualTo( 100 );
	}

	@Test
	void testUnbind() {
		Settings settings = new MapSettings();
		SettingsEventWatcher watcher = new SettingsEventWatcher();

		settings.bind( "key", "initial", watcher );
		assertThat( watcher.getEvents() ).hasSize( 1 );

		settings.unbind( "key", watcher );

		// Mutating setting should no longer notify watcher
		settings.set( "key", "changed" );
		assertThat( watcher.getEvents() ).hasSize( 1 );
	}

}
