package com.acromere.data;

import com.acromere.settings.SettingsEvent;
import com.acromere.settings.SettingsEventAssert;
import com.acromere.settings.SettingsEventWatcher;
import com.acromere.util.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

public class DataDataNodeSettingsTest {

	private MockDataNode node;

	private DataNodeSettings settings;

	@BeforeEach
	public void setup() {
		node = new MockDataNode();
		settings = new DataNodeSettings( node );
	}

	@Test
	public void testEventHandling() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.register( SettingsEvent.CHANGED, watcher );
		assertThat( watcher.getEvents().size() ).isEqualTo( 0 );

		node.setValue( "value", Double.MAX_VALUE );

		int index = 0;
		SettingsEventAssert.assertThat( watcher.getEvents().get( index++ ) ).hasValues( settings, SettingsEvent.CHANGED, settings.getPath(), "value", null, Double.MAX_VALUE );
		assertThat( watcher.getEvents().size() ).isEqualTo( index );
	}

	@Test
	public void testFlush() {
		assertThat( settings.flush() ).isEqualTo( settings );
	}

	@Test
	public void testExists() {
		assertFalse( settings.exists( "value" ) );

		settings.set( "value", Double.MAX_VALUE );
		assertTrue( settings.exists( "value" ) );

		settings.remove( "value" );
		assertFalse( settings.exists( "value" ) );
	}

	@Test
	public void testGetName() {
		assertThat( settings.getName() ).isEqualTo( node.getCollectionId() );
	}

	@Test
	public void testGetPath() {
		assertThat( settings.getPath() ).isEqualTo( node.getCollectionId() );
	}

	@Test
	public void testGetKeys() {
		node.setValue( "b", "B" );
		node.setValue( "a", "A" );
		node.setValue( "c", "C" );
		assertThat( settings.getKeys() ).contains( "a", "b", "c" );
	}

	@Test
	public void testSet() {
		assertNull( node.getValue( "value" ) );
		settings.set( "value", Double.MIN_VALUE );
		assertThat( node.<Double> getValue( "value" ) ).isEqualTo( Double.MIN_VALUE );
	}

	@Test
	public void testGet() {
		assertNull( node.getValue( "value" ) );
		node.setValue( "value", Double.MAX_VALUE );
		assertThat( settings.get( "value", Double.class ) ).isEqualTo( Double.MAX_VALUE );
	}

	@Test
	public void testRemove() {
		settings.set( "value", Double.MAX_VALUE );
		assertThat( node.<Double> getValue( "value" ) ).isEqualTo( Double.MAX_VALUE );

		settings.remove( "value" );
		assertNull( node.getValue( "value" ) );
	}

	@Test
	public void testGetWithDefault() {
		assertThat( settings.get( "unknown", new TypeReference<String>() {}, "fallback" ) ).isEqualTo( "fallback" );
		settings.set( "unknown", "actual" );
		assertThat( settings.get( "unknown", new TypeReference<String>() {}, "fallback" ) ).isEqualTo( "actual" );
	}

	@Test
	public void testUnregister() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.register( SettingsEvent.CHANGED, watcher );
		settings.unregister( SettingsEvent.CHANGED, watcher );

		node.setValue( "value", "newVal" );
		assertThat( watcher.getEvents() ).isEmpty();
	}

	@Test
	public void testGetEventHandlers() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.register( SettingsEvent.CHANGED, watcher );
		assertThat( settings.getEventHandlers() ).isNotEmpty();
	}

	@Test
	public void testUnsupportedOperations() {
		assertThatThrownBy( () -> settings.nodeExists( "path" ) ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.getNode( "path" ) ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.getNode( "parent", "name" ) ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.getNode( "path", Map.of() ) ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.getNodes() ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.copyFrom( settings ) ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.delete() ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.getDefaultValues() ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.setDefaultValues( Map.of() ) ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.loadDefaultValues( this, "path" ) ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.register( "key", e -> {} ) ).isInstanceOf( UnsupportedOperationException.class );
		assertThatThrownBy( () -> settings.unregister( "key", e -> {} ) ).isInstanceOf( UnsupportedOperationException.class );
	}

}
