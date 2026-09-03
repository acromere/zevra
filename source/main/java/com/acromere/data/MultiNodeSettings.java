package com.acromere.data;

import com.acromere.event.Event;
import com.acromere.event.EventHandler;
import com.acromere.event.EventHub;
import com.acromere.event.EventType;
import com.acromere.settings.Settings;
import com.acromere.settings.SettingsEvent;
import com.acromere.transaction.Txn;
import com.acromere.transaction.TxnException;
import com.acromere.util.TypeReference;
import lombok.CustomLog;

import java.util.*;

/**
 * This class is mainly used to set the values of multiple nodes at the same
 * time. It can also detect when all nodes have the same value for a particular
 * key. It will also return the keys common to all nodes, if any.
 */
@CustomLog
public class MultiNodeSettings implements Settings {

	private final Set<? extends DataNode> nodes;

	private final EventHub eventHub;

	public MultiNodeSettings( DataNode... nodes ) {
		this( Arrays.asList( nodes ) );
	}

	public MultiNodeSettings( Collection<? extends DataNode> nodes ) {
		this.nodes = new HashSet<>( nodes );
		this.eventHub = new EventHub();

		// NodeEvent.VALUE_CHANGED events need to be mapped to SettingsEvent.CHANGED events
		nodes.forEach( n -> n.register(
			DataNodeEvent.VALUE_CHANGED, e -> {
				if( nodes.contains( e.getNode() ) ) eventHub.dispatch( new SettingsEvent( this, SettingsEvent.CHANGED, ".", e.getKey(), e.getNewValue() ) );
			}
		) );
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public boolean nodeExists( String path ) {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	@Override
	public Settings getNode( String path ) {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	@Override
	public Settings getNode( String parent, String name ) {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	@Override
	public Settings getNode( String path, Map<String, String> values ) {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	@Override
	public List<String> getNodes() {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	/**
	 * Returns the keys common to all nodes.
	 *
	 * @return The set of common keys
	 */
	@Override
	public Set<String> getKeys() {
		// Create an initial set of keys
		Set<String> keys = new HashSet<>();
		if( !nodes.isEmpty() ) keys.addAll( nodes.iterator().next().getValueKeys() );

		// Remove any keys that are not in every node
		for( DataNode node : nodes ) {
			Set<String> valueKeys = node.getValueKeys();
			for( String key : new HashSet<>( keys ) ) {
				if( valueKeys.contains( key ) ) continue;
				keys.remove( key );
			}
			if( keys.isEmpty() ) break;
		}

		return keys;
	}

	@Override
	public boolean exists( String key ) {
		return getKeys().contains( key );
	}

	@Override
	public <T> T get( String key, TypeReference<T> type, T defaultValue ) {
		T value = null;
		if( !nodes.isEmpty() ) value = nodes.iterator().next().getValue( key );

		for( DataNode node : nodes ) {
			if( !Objects.equals( node.getValue( key ), value ) ) return defaultValue;
		}

		return value == null ? defaultValue : value;
	}

	@Override
	public Settings set( String key, Object value ) {
		try( Txn ignored = Txn.create() ) {
			nodes.forEach( n -> n.setValue( key, value ) );
		} catch( TxnException exception ) {
			log.atSevere().withCause( exception ).log( "Error setting value on multiple nodes" );
		}
		return this;
	}

	@Override
	public Settings copyFrom( Settings settings ) {
		throw new UnsupportedOperationException( "Copy not supported" );
	}

	@Override
	public Settings remove( String key ) {
		try( Txn ignored = Txn.create() ) {
			nodes.forEach( n -> n.setValue( key, null ) );
		} catch( TxnException exception ) {
			log.atSevere().withCause( exception ).log( "Error removing keys from multiple nodes" );
		} finally {
			Txn.reset();
		}
		return this;
	}

	@Override
	public Settings flush() {
		return this;
	}

	@Override
	public Settings delete() {
		throw new UnsupportedOperationException( "Delete not supported" );
	}

	@Override
	public Map<?, ?> getDefaultValues() {
		throw new UnsupportedOperationException( "Default values not supported" );
	}

	@Override
	public void setDefaultValues( Map<?, ?> defaults ) {
		throw new UnsupportedOperationException( "Default values not supported" );
	}

	@Override
	public void loadDefaultValues( Object source, String path ) {
		throw new UnsupportedOperationException( "Default values not supported" );
	}

	@Override
	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		eventHub.register( type, handler );
		return eventHub;
	}

	@Override
	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		eventHub.unregister( type, handler );
		return eventHub;
	}

	@Override
	public void register( String key, EventHandler<SettingsEvent> handler ) {
		throw new UnsupportedOperationException( "Use Node.register() instead" );
	}

	@Override
	public void unregister( String key, EventHandler<? extends SettingsEvent> handler ) {
		throw new UnsupportedOperationException( "Use Node.unregister() instead" );
	}

	@Override
	public Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		return eventHub.getEventHandlers();
	}

}
