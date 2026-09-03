package com.acromere.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataNodeComparatorTest {

	@Test
	void testCompareTo() {
		NamedDataNode a = new NamedDataNode();
		NamedDataNode b = new NamedDataNode();
		NodeComparator<MockDataNode> comparator = NodeComparator.of( "name" );

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
		NodeComparator<MockDataNode> comparator = NodeComparator.of( "name" );

		assertThat( comparator.compare( a, b ) ).isEqualTo( 0 );
	}

	@Test
	void testCompareToWithNullValues() {
		NamedDataNode a = new NamedDataNode();
		NamedDataNode b = new NamedDataNode();
		NodeComparator<MockDataNode> comparator = NodeComparator.of( "name" );

		a.setName( "a" );
		b.setName( null );
		assertThat( comparator.compare( a, b ) ).isEqualTo( -1 );

		a.setName( null );
		b.setName( "b" );
		assertThat( comparator.compare( a, b ) ).isEqualTo( 1 );
	}

}
