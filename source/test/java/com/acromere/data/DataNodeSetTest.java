package com.acromere.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DataNodeSetTest {

	private DataNodeSet<MockDataNode> set;

	@BeforeEach
	void setup() {
		set = new DataNodeSet<>( "test" );
	}

	@Test
	void testCircularReferenceCheck() {
		MockDataNode node = new MockDataNode();
		try {
			node.addItem( node );
			fail( "CircularReferenceException should be thrown" );
		} catch( CircularReferenceException exception ) {
			// Intentionally ignore exception
			assertThat( exception.getMessage() ).startsWith( "Circular reference detected" );
		}
	}

	@Test
	void testAdd() {
		assertThat( set.size() ).isEqualTo( 0 );
		assertFalse( set.isModified() );

		set.add( new MockDataNode() );
		assertThat( set.size() ).isEqualTo( 1 );
		assertTrue( set.isModified() );
	}

	@Test
	void testContains() {
		MockDataNode node = new MockDataNode();
		assertFalse( set.contains( node ) );
		set.add( node );
		assertTrue( set.contains( node ) );
		set.remove( node );
		assertFalse( set.contains( node ) );
	}

	@Test
	void testSize() {
		MockDataNode node = new MockDataNode();
		assertThat( set.size() ).isEqualTo( 0 );
		set.add( node );
		assertThat( set.size() ).isEqualTo( 1 );
		set.remove( node );
		assertThat( set.size() ).isEqualTo( 0 );
	}

	@Test
	void testAddAll() {
		Set<MockDataNode> nodes = Set.of( new MockDataNode( "a" ), new MockDataNode( "b" ) );
		assertThat( set.size() ).isEqualTo( 0 );
		set.addAll( nodes );
		assertThat( set.size() ).isEqualTo( 2 );
		set.removeAll( nodes );
		assertThat( set.size() ).isEqualTo( 0 );
	}

	@Test
	void testClearSet() {
		MockDataNode node = new MockDataNode();
		node.addItems( Set.of( new MockDataNode( "a" ), new MockDataNode( "b" ) ) );
		assertThat( node.getItems().size() ).isEqualTo( 2 );
		node.clearItems();
		assertThat( node.getItems().size() ).isEqualTo( 0 );
	}

}
