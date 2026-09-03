package com.acromere.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

	@Test
	void testIsEmptyAndIsNodeSetEmpty() {
		assertThat( set.isEmpty() ).isTrue();
		assertThat( set.isNodeSetEmpty() ).isTrue();

		MockDataNode node = new MockDataNode( "a" );
		set.add( node );
		assertThat( set.isEmpty() ).isFalse();
		assertThat( set.isNodeSetEmpty() ).isFalse();

		set.clear();
		assertThat( set.isEmpty() ).isTrue();
	}

	@Test
	void testIteratorAndForEach() {
		MockDataNode a = new MockDataNode( "a" );
		MockDataNode b = new MockDataNode( "b" );
		set.addAll( Set.of( a, b ) );

		List<MockDataNode> iterated = new ArrayList<>();
		for( MockDataNode node : set ) {
			iterated.add( node );
		}
		assertThat( iterated ).containsExactlyInAnyOrder( a, b );

		AtomicInteger count = new AtomicInteger();
		set.forEach( node -> count.incrementAndGet() );
		assertThat( count.get() ).isEqualTo( 2 );
	}

	@Test
	void testToArray() {
		MockDataNode a = new MockDataNode( "a" );
		MockDataNode b = new MockDataNode( "b" );
		set.addAll( Set.of( a, b ) );

		Object[] arr1 = set.toArray();
		assertThat( arr1 ).containsExactlyInAnyOrder( a, b );

		MockDataNode[] arr2 = set.toArray( new MockDataNode[0] );
		assertThat( arr2 ).containsExactlyInAnyOrder( a, b );

		MockDataNode[] arr3 = set.toArray( MockDataNode[]::new );
		assertThat( arr3 ).containsExactlyInAnyOrder( a, b );
	}

	@Test
	void testRetainAll() {
		MockDataNode a = new MockDataNode( "a" );
		MockDataNode b = new MockDataNode( "b" );
		MockDataNode c = new MockDataNode( "c" );
		set.addAll( Set.of( a, b, c ) );

		boolean changed = set.retainAll( Set.of( a, c ) );
		assertThat( changed ).isTrue();
		assertThat( (Set<MockDataNode>)set ).containsExactlyInAnyOrder( a, c );

		boolean unchanged = set.retainAll( Set.of( a, c ) );
		assertThat( unchanged ).isFalse();

		boolean emptyRetain = set.retainAll( Set.of() );
		assertThat( emptyRetain ).isTrue();
		assertThat( (Set<MockDataNode>)set ).isEmpty();
	}

	@Test
	void testContainsAll() {
		MockDataNode a = new MockDataNode( "a" );
		MockDataNode b = new MockDataNode( "b" );
		set.addAll( Set.of( a, b ) );

		assertThat( (Set<MockDataNode>)set ).containsAll( Set.of( a, b ) );
		assertThat( set.containsAll( Set.of( a, new MockDataNode( "c" ) ) ) ).isFalse();
	}

	@Test
	void testStreamsAndSpliterator() {
		MockDataNode a = new MockDataNode( "a" );
		MockDataNode b = new MockDataNode( "b" );
		set.addAll( Set.of( a, b ) );

		assertThat( set.stream().toList() ).containsExactlyInAnyOrder( a, b );
		assertThat( set.parallelStream().toList() ).containsExactlyInAnyOrder( a, b );
		assertThat( set.spliterator() ).isNotNull();
	}

	@Test
	void testToString() {
		assertThat( set.toString() ).isEqualTo( "DataNodeSet[test]" );
	}

	@Test
	void testModifyAllowed() {
		MockDataNode a = new MockDataNode( "a" );
		assertThat( set.modifyAllowed( a ) ).isTrue();
		assertThat( set.modifyAllowed( "non-node" ) ).isTrue();

		set.setSetModifyFilter( node -> "allowed".equals( node.getValue( "tag" ) ) );
		assertThat( set.modifyAllowed( a ) ).isFalse();

		a.setValue( "tag", "allowed" );
		assertThat( set.modifyAllowed( a ) ).isTrue();
	}

}
