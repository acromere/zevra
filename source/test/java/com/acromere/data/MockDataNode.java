package com.acromere.data;

import com.acromere.event.Event;

import java.util.Collection;
import java.util.Set;

class MockDataNode extends DataNode {

	public static final String MOCK_ID = "mock-id";

	public static final String ITEMS = "items";

	private final NodeWatcher watcher;

	MockDataNode() {
		this( null );
	}

	MockDataNode( String id ) {
		definePrimaryKey( MOCK_ID );
		if( id != null ) setMockId( id );
		addModifyingKeys( ITEMS, "key", "child", "a", "b", "c", "x", "y", "z" );
		register( Event.ANY, watcher = new NodeWatcher() );
	}

	public String getMockId() {
		return getValue( MOCK_ID );
	}

	public MockDataNode setMockId( String id ) {
		setValue( MOCK_ID, id );
		return this;
	}

	/**
	 * This method follows the pattern documented in {@link DataNodeSet}
	 *
	 * @return The set of items
	 */
	public Set<MockDataNode> getItems() {
		return getValues( ITEMS );
	}

	/**
	 * This method follows the pattern documented in {@link DataNodeSet}
	 *
	 * @param item The item to add
	 * @return This node
	 */
	public MockDataNode addItem( MockDataNode item ) {
		addToSet( ITEMS, item );
		return this;
	}

	/**
	 * This method follows the pattern documented in {@link DataNodeSet}
	 *
	 * @param item The item to remove
	 * @return This node
	 */
	public MockDataNode removeItem( MockDataNode item ) {
		removeFromSet( ITEMS, item );
		return this;
	}

	public MockDataNode addItems( Collection<MockDataNode> items ) {
		addToSet( ITEMS, items );
		return this;
	}

	public MockDataNode removeItems( Collection<MockDataNode> items ) {
		removeFromSet( ITEMS, items );
		return this;
	}

	public MockDataNode clearItems() {
		clearSet( ITEMS );
		return this;
	}

	public Event event( int index ) {
		return watcher.getEvents().get( index );
	}

	public NodeWatcher getWatcher() {
		return watcher;
	}

	public int getEventCount() {
		return watcher.getEvents().size();
	}

}
