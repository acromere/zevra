package com.acromere.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataDataNodeEventTest {

	@Test
	void testEquals() {
		DataNode node = new MockDataNode();
		DataNodeEvent event1 = new DataNodeEvent( node, DataNodeEvent.NODE_CHANGED );
		DataNodeEvent event2 = new DataNodeEvent( node, DataNodeEvent.NODE_CHANGED );
		assertThat( event1.equals( event2 ) ).isEqualTo( true );
		assertThat( event2.equals( event1 ) ).isEqualTo( true );
	}

	@Test
	void testEqualsWithValueKey() {
		DataNode node = new MockDataNode();
		DataNodeEvent event1 = new DataNodeEvent( node, DataNodeEvent.VALUE_CHANGED, "a", null, "1" );
		DataNodeEvent event2 = new DataNodeEvent( node, DataNodeEvent.VALUE_CHANGED, "a", "1", "5" );
		assertThat( event1.equals( event2 ) ).isEqualTo( true );
		assertThat( event2.equals( event1 ) ).isEqualTo( true );
	}

}
