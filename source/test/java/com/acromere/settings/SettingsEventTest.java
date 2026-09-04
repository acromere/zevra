package com.acromere.settings;

import com.acromere.event.Event;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettingsEventTest {

	@Test
	void testEventHierarchy() {
		assertThat( SettingsEvent.SETTINGS.getParentEventType() ).isEqualTo( Event.ANY );
		assertThat( SettingsEvent.ANY ).isEqualTo( SettingsEvent.SETTINGS );
		assertThat( SettingsEvent.SAVED.getParentEventType() ).isEqualTo( SettingsEvent.SETTINGS );
		assertThat( SettingsEvent.CHANGED.getParentEventType() ).isEqualTo( SettingsEvent.SETTINGS );
		assertThat( SettingsEvent.LOADED.getParentEventType() ).isEqualTo( SettingsEvent.SETTINGS );
	}

	@Test
	void testThreeArgumentConstructor() {
		Settings settings = new MapSettings();
		SettingsEvent event = new SettingsEvent( settings, SettingsEvent.LOADED, "/test/path" );

		assertThat( event.getSource() ).isSameAs( settings );
		assertThat( event.getSettings() ).isSameAs( settings );
		assertThat( event.getEventType() ).isEqualTo( SettingsEvent.LOADED );
		assertThat( event.getPath() ).isEqualTo( "/test/path" );
		assertThat( event.getKey() ).isNull();
		assertThat( event.getOldValue() ).isNull();
		assertThat( event.getNewValue() ).isNull();
	}

	@Test
	void testFiveArgumentConstructor() {
		Settings settings = new MapSettings();
		SettingsEvent event = new SettingsEvent( settings, SettingsEvent.CHANGED, "/test/path", "name", "John" );

		assertThat( event.getSource() ).isSameAs( settings );
		assertThat( event.getSettings() ).isSameAs( settings );
		assertThat( event.getEventType() ).isEqualTo( SettingsEvent.CHANGED );
		assertThat( event.getPath() ).isEqualTo( "/test/path" );
		assertThat( event.getKey() ).isEqualTo( "name" );
		assertThat( event.getOldValue() ).isNull();
		assertThat( event.getNewValue() ).isEqualTo( "John" );
	}

	@Test
	void testSixArgumentConstructor() {
		Settings settings = new MapSettings();
		SettingsEvent event = new SettingsEvent( settings, SettingsEvent.CHANGED, "/test/path", "count", 1, 2 );

		assertThat( event.getSource() ).isSameAs( settings );
		assertThat( event.getSettings() ).isSameAs( settings );
		assertThat( event.getEventType() ).isEqualTo( SettingsEvent.CHANGED );
		assertThat( event.getPath() ).isEqualTo( "/test/path" );
		assertThat( event.getKey() ).isEqualTo( "count" );
		assertThat( event.getOldValue() ).isEqualTo( 1 );
		assertThat( event.getNewValue() ).isEqualTo( 2 );
	}

	@Test
	void testToString() {
		Settings settings = new MapSettings();

		SettingsEvent loadedEvent = new SettingsEvent( settings, SettingsEvent.LOADED, "/test/path" );
		assertThat( loadedEvent.toString() ).isEqualTo( "MapSettings > SettingsEvent : LOADED path=/test/path" );

		SettingsEvent changedEvent = new SettingsEvent( settings, SettingsEvent.CHANGED, "/test/path", "key1", "oldVal", "newVal" );
		assertThat( changedEvent.toString() ).isEqualTo( "MapSettings > SettingsEvent : CHANGED path=/test/path key=key1 old=oldVal new=newVal" );

		SettingsEvent emptyEvent = new SettingsEvent( settings, SettingsEvent.SETTINGS, null );
		assertThat( emptyEvent.toString() ).isEqualTo( "MapSettings > SettingsEvent : SETTINGS" );
	}

	@Test
	void testSettingsEventAssert() {
		Settings settings = new MapSettings();
		Settings otherSettings = new MapSettings();
		SettingsEvent event = new SettingsEvent( settings, SettingsEvent.CHANGED, "/path", "key", "old", "new" );

		// Matching assertion passes
		SettingsEventAssert.assertThat( event ).hasValues( settings, SettingsEvent.CHANGED, "/path", "key", "old", "new" );

		// Mismatches fail with appropriate assertion error
		assertThatThrownBy( () -> SettingsEventAssert.assertThat( event ).hasValues( otherSettings, SettingsEvent.CHANGED, "/path", "key", "old", "new" ) )
			.isInstanceOf( AssertionError.class );

		assertThatThrownBy( () -> SettingsEventAssert.assertThat( event ).hasValues( settings, SettingsEvent.SAVED, "/path", "key", "old", "new" ) )
			.isInstanceOf( AssertionError.class );

		assertThatThrownBy( () -> SettingsEventAssert.assertThat( event ).hasValues( settings, SettingsEvent.CHANGED, "/other", "key", "old", "new" ) )
			.isInstanceOf( AssertionError.class );

		assertThatThrownBy( () -> SettingsEventAssert.assertThat( event ).hasValues( settings, SettingsEvent.CHANGED, "/path", "otherKey", "old", "new" ) )
			.isInstanceOf( AssertionError.class );

		assertThatThrownBy( () -> SettingsEventAssert.assertThat( event ).hasValues( settings, SettingsEvent.CHANGED, "/path", "key", "wrongOld", "new" ) )
			.isInstanceOf( AssertionError.class );

		assertThatThrownBy( () -> SettingsEventAssert.assertThat( event ).hasValues( settings, SettingsEvent.CHANGED, "/path", "key", "old", "wrongNew" ) )
			.isInstanceOf( AssertionError.class );
	}

}
