package com.acromere.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataDataNodeEventTest {

	@Test
	void testConstructorsAndGetters() {
		DataNode node = new MockDataNode();

		DataNodeEvent event1 = new DataNodeEvent( node, DataNodeEvent.NODE_CHANGED );
		assertThat( (DataNode)event1.getNode() ).isSameAs( node );
		assertThat( event1.getEventType() ).isEqualTo( DataNodeEvent.NODE_CHANGED );
		assertThat( (String)event1.getSetKey() ).isNull();
		assertThat( (String)event1.getKey() ).isNull();
		assertThat( (Object)event1.getOldValue() ).isNull();
		assertThat( (Object)event1.getNewValue() ).isNull();

		DataNodeEvent event2 = new DataNodeEvent( node, DataNodeEvent.VALUE_CHANGED, "k1", "vOld", "vNew" );
		assertThat( (DataNode)event2.getNode() ).isSameAs( node );
		assertThat( event2.getEventType() ).isEqualTo( DataNodeEvent.VALUE_CHANGED );
		assertThat( event2.getSetKey() ).isNull();
		assertThat( event2.getKey() ).isEqualTo( "k1" );
		assertThat( (String)event2.getOldValue() ).isEqualTo( "vOld" );
		assertThat( (String)event2.getNewValue() ).isEqualTo( "vNew" );

		DataNodeEvent event3 = new DataNodeEvent( node, DataNodeEvent.ADDED, "items", "k2", "vOld2", "vNew2" );
		assertThat( (DataNode)event3.getNode() ).isSameAs( node );
		assertThat( event3.getEventType() ).isEqualTo( DataNodeEvent.ADDED );
		assertThat( event3.getSetKey() ).isEqualTo( "items" );
		assertThat( event3.getKey() ).isEqualTo( "k2" );
		assertThat( (String)event3.getOldValue() ).isEqualTo( "vOld2" );
		assertThat( (String)event3.getNewValue() ).isEqualTo( "vNew2" );

		DataNode target = new MockDataNode();
		DataNodeEvent copy = new DataNodeEvent( target, event3 );
		assertThat( (DataNode)copy.getNode() ).isSameAs( target );
		assertThat( copy.getEventType() ).isEqualTo( DataNodeEvent.ADDED );
		assertThat( copy.getSetKey() ).isEqualTo( "items" );
		assertThat( copy.getKey() ).isEqualTo( "k2" );
		assertThat( (String)copy.getOldValue() ).isEqualTo( "vOld2" );
		assertThat( (String)copy.getNewValue() ).isEqualTo( "vNew2" );
	}

	@Test
	void testCollapseUp() {
		DataNode node = new MockDataNode();
		DataNodeEvent valueChanged = new DataNodeEvent( node, DataNodeEvent.VALUE_CHANGED );
		assertThat( valueChanged.collapseUp() ).isTrue();

		DataNodeEvent nodeChanged = new DataNodeEvent( node, DataNodeEvent.NODE_CHANGED );
		assertThat( nodeChanged.collapseUp() ).isFalse();

		DataNodeEvent added = new DataNodeEvent( node, DataNodeEvent.ADDED );
		assertThat( added.collapseUp() ).isFalse();
	}

	@Test
	void testToString() {
		DataNode node = new MockDataNode();
		DataNodeEvent event1 = new DataNodeEvent( node, DataNodeEvent.NODE_CHANGED );
		assertThat( event1.toString() ).contains( "DataNodeEvent[ type=NODE_CHANGED" );

		DataNodeEvent event2 = new DataNodeEvent( node, DataNodeEvent.VALUE_CHANGED, "set1", "k1", "oldVal", "newVal" );
		String str = event2.toString();
		assertThat( str ).contains( "setKey=set1" ).contains( "key=k1" ).contains( "oldValue=oldVal" ).contains( "newValue=newVal" );
	}

	@Test
	void testHashCode() {
		DataNode node1 = new MockDataNode();
		DataNodeEvent event1 = new DataNodeEvent( node1, DataNodeEvent.NODE_CHANGED );
		DataNodeEvent event2 = new DataNodeEvent( node1, DataNodeEvent.NODE_CHANGED );
		assertThat( event1.hashCode() ).isEqualTo( event2.hashCode() );

		DataNodeEvent event3 = new DataNodeEvent( node1, DataNodeEvent.VALUE_CHANGED, "k", "old", "new" );
		DataNodeEvent event4 = new DataNodeEvent( node1, DataNodeEvent.VALUE_CHANGED, "k", "old2", "new2" );
		assertThat( event3.hashCode() ).isEqualTo( event4.hashCode() );
	}

	@Test
	void testEquals() {
		DataNode node1 = new MockDataNode( "n1" );
		DataNode node2 = new MockDataNode( "n2" );
		DataNodeEvent event1 = new DataNodeEvent( node1, DataNodeEvent.NODE_CHANGED );
		DataNodeEvent event2 = new DataNodeEvent( node1, DataNodeEvent.NODE_CHANGED );
		DataNodeEvent eventDiffType = new DataNodeEvent( node1, DataNodeEvent.ADDED );
		DataNodeEvent eventDiffNode = new DataNodeEvent( node2, DataNodeEvent.NODE_CHANGED );

		assertThat( event1.equals( event2 ) ).isTrue();
		assertThat( event2.equals( event1 ) ).isTrue();
		assertThat( event1.equals( event1 ) ).isTrue();
		assertThat( event1.equals( null ) ).isFalse();
		assertThat( event1.equals( "other" ) ).isFalse();
		assertThat( event1.equals( eventDiffType ) ).isFalse();
		assertThat( event1.equals( eventDiffNode ) ).isFalse();
	}

	@Test
	void testEqualsWithValueKey() {
		DataNode node = new MockDataNode();
		DataNodeEvent event1 = new DataNodeEvent( node, DataNodeEvent.VALUE_CHANGED, "a", null, "1" );
		DataNodeEvent event2 = new DataNodeEvent( node, DataNodeEvent.VALUE_CHANGED, "a", "1", "5" );
		DataNodeEvent event3 = new DataNodeEvent( node, DataNodeEvent.VALUE_CHANGED, "b", "1", "5" );

		assertThat( event1.equals( event2 ) ).isTrue();
		assertThat( event2.equals( event1 ) ).isTrue();
		assertThat( event1.equals( event3 ) ).isFalse();
	}

}
