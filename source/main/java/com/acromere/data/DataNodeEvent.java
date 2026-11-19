package com.acromere.data;

import com.acromere.event.EventType;
import com.acromere.transaction.TxnEvent;
import lombok.Getter;

import java.util.Objects;

/**
 * Event fired to {@link com.acromere.transaction.TxnEventTarget targets} when a
 * {@link DataNode} instance is modified.
 * <p>
 * The {@link #ANY} event type is the root of all data node event types.
 */
public class DataNodeEvent extends TxnEvent {

	/** Root event type for all data node events. */
	public static final EventType<DataNodeEvent> ANY = new EventType<>( TxnEvent.ANY, "NODE" );

	/** Event type fired when a node becomes modified. */
	public static final EventType<DataNodeEvent> MODIFIED = new EventType<>( ANY, "MODIFIED" );

	/** Event type fired when a node becomes unmodified. */
	public static final EventType<DataNodeEvent> UNMODIFIED = new EventType<>( ANY, "UNMODIFIED" );

	/** Event type fired when a node is added to a parent. */
	public static final EventType<DataNodeEvent> ADDED = new EventType<>( ANY, "ADDED" );

	/** Event type fired when a node is removed from a parent. */
	public static final EventType<DataNodeEvent> REMOVED = new EventType<>( ANY, "REMOVED" );

	/** Event type fired when a child is added to a node. */
	public static final EventType<DataNodeEvent> CHILD_ADDED = new EventType<>( ANY, "CHILD_ADDED" );

	/** Event type fired when a child is removed from a node. */
	public static final EventType<DataNodeEvent> CHILD_REMOVED = new EventType<>( ANY, "CHILD_REMOVED" );

	/** Event type fired when a node is changed. */
	public static final EventType<DataNodeEvent> NODE_CHANGED = new EventType<>( ANY, "NODE_CHANGED" );

	/** Event type fired when a node's parent changes. */
	public static final EventType<DataNodeEvent> PARENT_CHANGED = new EventType<>( ANY, "PARENT_CHANGED" );

	/** Event type fired when a node's value changes. */
	public static final EventType<DataNodeEvent> VALUE_CHANGED = new EventType<>( ANY, "VALUE_CHANGED" );

	/**
	 * The set key of the value, or {@code null} if the value is not stored in a set.
	 */
	@Getter
	private final String setKey;

	/**
	 * The key of the value, or {@code null} if the event has no key.
	 */
	@Getter
	private final String key;

	/**
	 * The old value of the event, or {@code null} if the event has no old value.
	 */
	private final Object oldValue;

	/**
	 * The new value of the event, or {@code null} if the event has no new value.
	 */
	private final Object newValue;

	/**
	 * Create a data node event of the given type fired by the given node.
	 *
	 * @param node The node the event is fired by
	 * @param type The event type
	 */
	public DataNodeEvent( DataNode node, EventType<? extends DataNodeEvent> type ) {
		this( node, type, null, null, null );
	}

	/**
	 * Create a data node event with the given key and values.
	 *
	 * @param node The node the event is fired by
	 * @param type The event type
	 * @param key The key of the value
	 * @param oldValue The old value
	 * @param newValue The new value
	 */
	public DataNodeEvent( DataNode node, EventType<? extends DataNodeEvent> type, String key, Object oldValue, Object newValue ) {
		this( node, type, null, key, oldValue, newValue );
	}

	/**
	 * Create a data node event with the given set key, key and values.
	 *
	 * @param node The node the event is fired by
	 * @param type The event type
	 * @param setKey The set key of the value
	 * @param key The key of the value
	 * @param oldValue The old value
	 * @param newValue The new value
	 */
	public DataNodeEvent( DataNode node, EventType<? extends DataNodeEvent> type, String setKey, String key, Object oldValue, Object newValue ) {
		super( node, type );
		this.setKey = setKey;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * Create a data node event that copies the given event fired by the given node.
	 *
	 * @param node The node the event is fired by
	 * @param event The event to copy
	 */
	public DataNodeEvent( DataNode node, DataNodeEvent event ) {
		super( node, event.getEventType() );
		this.setKey = event.getSetKey();
		this.key = event.getKey();
		this.oldValue = event.getOldValue();
		this.newValue = event.getNewValue();
	}

	/**
	 * Get the data node the event was fired by.
	 *
	 * @param <T> The node type
	 * @return the data node
	 */
	@SuppressWarnings( "unchecked" )
	public <T extends DataNode> T getNode() {
		return (T)getSource();
	}

	/**
	 * Get the old value of the event.
	 *
	 * @param <T> The value type
	 * @return the old value
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T getOldValue() {
		return (T)oldValue;
	}

	/**
	 * Get the new value of the event.
	 *
	 * @param <T> The value type
	 * @return the new value
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T getNewValue() {
		return (T)newValue;
	}

	/**
	 * Determine if the event collapses up the parent chain.
	 *
	 * @return {@code true} if the event type is {@link #VALUE_CHANGED}
	 */
	public boolean collapseUp() {
		return getEventType() == DataNodeEvent.VALUE_CHANGED;
	}

	/**
	 * Get the event type of the event.
	 *
	 * @return the event type
	 */
	@SuppressWarnings( "unchecked" )
	public EventType<? extends DataNodeEvent> getEventType() {
		return (EventType<? extends DataNodeEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append( getClass().getSimpleName() );
		builder.append( "[ " );
		builder.append( "type=" );
		builder.append( getEventType() );
		builder.append( ", node=" );
		builder.append( (DataNode)getNode() );
		if( setKey != null ) builder.append( ", setKey=" ).append( setKey );
		if( key != null ) {
			builder.append( ", key=" ).append( key );
			builder.append( ", oldValue=" ).append( oldValue );
			builder.append( ", newValue=" ).append( newValue );
		}
		builder.append( " ]" );

		return builder.toString();
	}

	@Override
	public int hashCode() {
		int code = 0;

		if( getNode() != null ) code |= getNode().hashCode();
		if( getEventType() != null ) code |= getEventType().hashCode();
		if( key != null ) code |= key.hashCode();

		return code;
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof DataNodeEvent that) ) return false;
		return Objects.equals( this.getNode(), that.getNode() ) && Objects.equals( this.getEventType(), that.getEventType() ) && Objects.equals( this.key, that.key );
	}

}
