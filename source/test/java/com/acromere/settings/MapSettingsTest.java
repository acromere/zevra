package com.acromere.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MapSettingsTest extends BaseSettingsTest {

	@BeforeEach
	void setup() {
		settings = new MapSettings();
	}

	@Test
	void testDefaultConstructor() {
		MapSettings mapSettings = new MapSettings();
		assertThat( mapSettings.getPath() ).isEqualTo( "/" );
		assertThat( mapSettings.getName() ).isEqualTo( "" );
	}

	@Test
	void testFlushReturnsSelf() {
		assertThat( settings.flush() ).isSameAs( settings );
	}

	@Test
	void testDeleteClearsValuesAndRemovesFromRoot() {
		Settings child = settings.getNode( "childNode" );
		child.set( "a", "1" );
		assertThat( child.getKeys() ).containsExactly( "a" );
		assertThat( settings.nodeExists( "childNode" ) ).isTrue();

		child.delete();
		assertThat( child.getKeys() ).isEmpty();
		assertThat( settings.nodeExists( "childNode" ) ).isFalse();
	}

	@Test
	void testMultipleChildNodes() {
		settings.getNode( "/parent/child1" ).set( "k", "v" );
		settings.getNode( "/parent/child2" ).set( "k", "v" );
		assertThat( settings.getNode( "/parent" ).getNodes() ).containsExactlyInAnyOrder( "child1", "child2" );
	}

}
