package com.acromere.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DataDataNodeComparatorTest {

	@Test
	void testCompareTo() {
		NamedDataNode a = new NamedDataNode();
		NamedDataNode b = new NamedDataNode();
		DataNodeComparator<MockDataNode> comparator = DataNodeComparator.of( "name" );

		a.setName( "a" );
		b.setName( "b" );
		assertThat( comparator.compare( a, b ) ).isEqualTo( -1 );
		assertThat( comparator.compare( b, a ) ).isEqualTo( 1 );
		a.setName( "c" );
		b.setName( "c" );
		assertThat( comparator.compare( a, b ) ).isEqualTo( 0 );
		assertThat( comparator.compare( b, a ) ).isEqualTo( 0 );
	}

	@Test
	void testCompareToWithMissingValues() {
		NamedDataNode a = new NamedDataNode();
		NamedDataNode b = new NamedDataNode();
		DataNodeComparator<MockDataNode> comparator = DataNodeComparator.of( "name" );

		assertThat( comparator.compare( a, b ) ).isEqualTo( 0 );
	}

	@Test
	void testCompareToWithNullValues() {
		NamedDataNode a = new NamedDataNode();
		NamedDataNode b = new NamedDataNode();
		DataNodeComparator<MockDataNode> comparator = DataNodeComparator.of( "name" );

		a.setName( "a" );
		b.setName( null );
		assertThat( comparator.compare( a, b ) ).isEqualTo( -1 );

		a.setName( null );
		b.setName( "b" );
		assertThat( comparator.compare( a, b ) ).isEqualTo( 1 );
	}

	@Test
	void testOfListAndKeys() {
		List<String> keys = List.of( "name", "category" );
		DataNodeComparator<MockDataNode> comparator = DataNodeComparator.of( keys );
		assertThat( comparator.keys() ).isEqualTo( keys );
	}

	@Test
	void testCompareMultipleKeys() {
		MockDataNode a = new MockDataNode();
		MockDataNode b = new MockDataNode();
		DataNodeComparator<MockDataNode> comparator = DataNodeComparator.of( "group", "name" );

		a.setValue( "group", "alpha" );
		b.setValue( "group", "alpha" );
		a.setValue( "name", "apple" );
		b.setValue( "name", "banana" );

		assertThat( comparator.compare( a, b ) ).isNegative();
		assertThat( comparator.compare( b, a ) ).isPositive();

		b.setValue( "name", "apple" );
		assertThat( comparator.compare( a, b ) ).isEqualTo( 0 );
	}

	@Test
	void testCompareEmptyKeys() {
		MockDataNode a = new MockDataNode();
		MockDataNode b = new MockDataNode();
		DataNodeComparator<MockDataNode> comparator = DataNodeComparator.of( List.of() );
		assertThat( comparator.compare( a, b ) ).isEqualTo( 0 );
	}

}
