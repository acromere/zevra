package com.acromere.data;

import com.acromere.event.EventType;
import com.acromere.transaction.TxnEvent;
import lombok.Getter;

import java.util.Objects;

public class DataNodeEvent extends TxnEvent {

	public static final EventType<DataNodeEvent> ANY = new EventType<>( TxnEvent.ANY, "NODE" );

	public static final EventType<DataNodeEvent> MODIFIED = new EventType<>( ANY, "MODIFIED" );

	public static final EventType<DataNodeEvent> UNMODIFIED = new EventType<>( ANY, "UNMODIFIED" );

	public static final EventType<DataNodeEvent> ADDED = new EventType<>( ANY, "ADDED" );

	public static final EventType<DataNodeEvent> REMOVED = new EventType<>( ANY, "REMOVED" );

	public static final EventType<DataNodeEvent> CHILD_ADDED = new EventType<>( ANY, "CHILD_ADDED" );

	public static final EventType<DataNodeEvent> CHILD_REMOVED = new EventType<>( ANY, "CHILD_REMOVED" );

	public static final EventType<DataNodeEvent> NODE_CHANGED = new EventType<>( ANY, "NODE_CHANGED" );

	public static final EventType<DataNodeEvent> PARENT_CHANGED = new EventType<>( ANY, "PARENT_CHANGED" );

	public static final EventType<DataNodeEvent> VALUE_CHANGED = new EventType<>( ANY, "VALUE_CHANGED" );

	@Getter
	private final String setKey;

	@Getter
	private final String key;

	private final Object oldValue;

	private final Object newValue;

	public DataNodeEvent( DataNode node, EventType<? extends DataNodeEvent> type ) {
		this( node, type, null, null, null );
	}

	public DataNodeEvent( DataNode node, EventType<? extends DataNodeEvent> type, String key, Object oldValue, Object newValue ) {
		this( node, type, null, key, oldValue, newValue );
	}

	public DataNodeEvent( DataNode node, EventType<? extends DataNodeEvent> type, String setKey, String key, Object oldValue, Object newValue ) {
		super( node, type );
		this.setKey = setKey;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public DataNodeEvent( DataNode node, DataNodeEvent event ) {
		super( node, event.getEventType() );
		this.setKey = event.getSetKey();
		this.key = event.getKey();
		this.oldValue = event.getOldValue();
		this.newValue = event.getNewValue();
	}

	@SuppressWarnings( "unchecked" )
	public <T extends DataNode> T getNode() {
		return (T)getSource();
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getOldValue() {
		return (T)oldValue;
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getNewValue() {
		return (T)newValue;
	}

	public boolean collapseUp() {
		return getEventType() == DataNodeEvent.VALUE_CHANGED;
	}

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
