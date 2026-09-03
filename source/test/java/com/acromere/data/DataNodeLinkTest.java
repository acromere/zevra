package com.acromere.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataNodeLinkTest {

	private MockDataNode data;

	@BeforeEach
	void setup() {
		data = new MockDataNode( "a" );
	}

	@Test
	void testAddRemove() {
		MockDataNode a = new MockDataNode( "a" );
		MockDataNode peer = new MockDataNode( "peer" );

		data.setValue( "a", a );
		data.setModified( false );
		assertThat( data.isModified() ).isFalse();

		DataNodeLink<MockDataNode> l = new DataNodeLink<>( a );
		peer.setValue( "l", l );
		assertThat( peer.isModified() ).isFalse();
		assertThat( data.isModified() ).isFalse();
		// Node 'l' should belong to peer
		assertThat( peer.<Object> getValue( "l" ) ).isEqualTo( l );
		// Node 'a' should still belong to 'data'
		assertThat( data.<Object> getValue( "a" ) ).isEqualTo( a );
		assertThat( peer.<DataNodeLink<MockDataNode>> getValue( "l" ).getNode() ).isEqualTo( a );

		peer.setValue( "l", null );
		assertThat( peer.isModified() ).isFalse();
		assertThat( data.isModified() ).isFalse();
		assertThat( peer.<Object> getValue( "l" ) ).isNull();
		assertThat( data.<MockDataNode> getValue( "a" ) ).isEqualTo( a );
	}

	@Test
	void testModified() {
		MockDataNode a = new MockDataNode( "a" );
		MockDataNode peer = new MockDataNode( "peer" );

		data.setValue( "a", a );
		data.setModified( false );
		assertThat( data.isModified() ).isFalse();

		DataNodeLink<MockDataNode> l = new DataNodeLink<>( a );
		peer.setValue( "l", l );
		assertThat( peer.isModified() ).isFalse();

		a.setValue( "x", 2.4 );
		assertThat( a.isModified() ).isTrue();
		assertThat( data.isModified() ).isTrue();
		assertThat( l.isModified() ).isFalse();
		assertThat( peer.isModified() ).isFalse();
	}

	@Test
	void testOf() {
		MockDataNode node = new MockDataNode( "n" );
		DataNodeLink<MockDataNode> link = DataNodeLink.of( node );
		assertThat( link.getNode() ).isSameAs( node );
	}

	@Test
	void testToString() {
		MockDataNode node = new MockDataNode( "n" );
		DataNodeLink<MockDataNode> link = DataNodeLink.of( node );
		assertThat( link.toString() ).isEqualTo( "NodeLink@" + node );
	}

	@Test
	void testId() {
		MockDataNode node = new MockDataNode( "n" );
		DataNodeLink<MockDataNode> link = DataNodeLink.of( node );
		assertThat( link.getId() ).isNotNull();
		link.setId( "custom-link-id" );
		assertThat( link.getId() ).isEqualTo( "custom-link-id" );
	}

}
