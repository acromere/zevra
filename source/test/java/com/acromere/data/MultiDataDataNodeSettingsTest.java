package com.acromere.data;

import com.acromere.settings.SettingsEvent;
import com.acromere.settings.SettingsEventAssert;
import com.acromere.settings.SettingsEventWatcher;
import com.acromere.transaction.Txn;
import com.acromere.transaction.TxnOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiDataDataNodeSettingsTest {

	private MockDataNode node1;

	private MockDataNode node2;

	private MockDataNode node3;

	private MultiNodeSettings settings;

	@BeforeEach
	public void setup() {
		node1 = new MockDataNode( "a" );
		node2 = new MockDataNode( "b" );
		node3 = new MockDataNode( "c" );

		node1.setValue( "color", "blue" );
		node2.setValue( "color", "blue" );
		node3.setValue( "color", "blue" );

		node1.setValue( "size", "S" );
		node2.setValue( "size", "M" );
		node3.setValue( "size", "L" );

		node2.setValue( "count", 42 );

		settings = new MultiNodeSettings( node1, node2, node3 );
	}

	@Test
	void testCreateWithCollection() {
		MultiNodeSettings settings = new MultiNodeSettings( Set.of( node1, node2, node3 ) );

		// These are MOCK_ID, "color" and "size"
		assertThat( settings.getKeys().size() ).isEqualTo( 3 );
	}

	@Test
	void testGetName() {
		assertThat( settings.getName() ).isNull();
	}

	@Test
	void testGetPath() {
		assertThat( settings.getPath() ).isNull();
	}

	@Test
	void testGetKeys() {
		assertThat( settings.getKeys() ).contains( MockDataNode.MOCK_ID, "color", "size" );
	}

	@Test
	void testExists() {
		assertTrue( settings.exists( "color" ) );
		assertTrue( settings.exists( "size" ) );
		assertFalse( settings.exists( "count" ) );
	}

	@Test
	void testGet() {
		assertThat( settings.get( "color" ) ).isEqualTo( "blue" );
		assertThat( settings.get( "size", "default" ) ).isEqualTo( "default" );
		assertThat( settings.get( "size" ) ).isNull();
		assertThat( settings.get( "count" ) ).isNull();
	}

	@Test
	void testSet() {
		assertThat( node1.<Integer> getValue( "count" ) ).isNull();
		assertThat( node2.<Integer> getValue( "count" ) ).isEqualTo( 42 );
		assertThat( node3.<Integer> getValue( "count" ) ).isNull();
		settings.set( "count", 37 );
		assertThat( node1.<Integer> getValue( "count" ) ).isEqualTo( 37 );
		assertThat( node2.<Integer> getValue( "count" ) ).isEqualTo( 37 );
		assertThat( node3.<Integer> getValue( "count" ) ).isEqualTo( 37 );
	}

	@Test
	void testSetWithNestedTxn() throws Exception {
		assertThat( node1.<Integer> getValue( "count" ) ).isNull();
		assertThat( node2.<Integer> getValue( "count" ) ).isEqualTo( 42 );
		assertThat( node3.<Integer> getValue( "count" ) ).isNull();
		try( Txn ignored = Txn.create( true ) ) {
			settings.set( "temp", "temp-value" );
			settings.set( "count", 37 );
			Txn.submit( new TxnOperation( e -> {} ) {

				@Override
				protected TxnOperation commit() {
					return this;
				}

				@Override
				protected TxnOperation revert() {
					return this;
				}
			} );
		}
		assertThat( node1.<Integer> getValue( "count" ) ).isEqualTo( 37 );
		assertThat( node2.<Integer> getValue( "count" ) ).isEqualTo( 37 );
		assertThat( node3.<Integer> getValue( "count" ) ).isEqualTo( 37 );
	}

	@Test
	void testRemove() {
		assertTrue( node1.exists( "size" ) );
		assertTrue( node2.exists( "size" ) );
		assertTrue( node3.exists( "size" ) );
		settings.remove( "size" );
		assertFalse( node1.exists( "size" ) );
		assertFalse( node2.exists( "size" ) );
		assertFalse( node3.exists( "size" ) );
	}

	@Test
	void testFlush() {
		assertThat( settings.flush() ).isEqualTo( settings );
	}

	@Test
	void testEmptyNodes() {
		MultiNodeSettings emptySettings = new MultiNodeSettings( List.of() );
		assertThat( emptySettings.getKeys() ).isEmpty();
		assertThat( emptySettings.exists( "color" ) ).isFalse();
		assertThat( emptySettings.get( "color", "default" ) ).isEqualTo( "default" );
		assertThat( emptySettings.get( "color" ) ).isNull();
	}

	@Test
	void testEventHandling() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.register( SettingsEvent.CHANGED, watcher );
		assertThat( watcher.getEvents() ).isEmpty();

		node1.setValue( "color", "red" );

		assertThat( watcher.getEvents() ).hasSize( 1 );
		SettingsEventAssert.assertThat( watcher.getEvents().get( 0 ) ).hasValues( settings, SettingsEvent.CHANGED, ".", "color", null, "red" );

		settings.unregister( SettingsEvent.CHANGED, watcher );
		node1.setValue( "color", "green" );
		assertThat( watcher.getEvents() ).hasSize( 1 );
	}

	@Test
	void testGetEventHandlers() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.register( SettingsEvent.CHANGED, watcher );
		assertThat( settings.getEventHandlers() ).isNotEmpty();
	}

	@Test
	void testUnsupportedOperations() {
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
