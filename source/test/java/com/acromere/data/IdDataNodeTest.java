package com.acromere.data;

import com.acromere.transaction.Txn;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IdDataNodeTest {

	@Test
	void testId() {
		assertThat( new MockIdDataNode().getId() ).isNotNull();
	}

	@Test
	void testConstructor() {
		assertThat( new MockIdDataNode() ).isNotNull();
	}

	@Test
	void testConstructorWithTransaction() throws Exception {
		IdDataNode node;
		try( Txn ignore = Txn.create() ) {
			node = new MockIdDataNode();
			// The id will be null until the txn is complete
			assertThat( node.getId() ).isNull();
		}
		assertThat( node.getId() ).isNotNull();
	}

	@Test
	void testSetId() {
		IdDataNode node = new MockIdDataNode();
		MockIdDataNode result = node.setId( "test-custom-id" );
		assertThat( result ).isSameAs( node );
		assertThat( node.getId() ).isEqualTo( "test-custom-id" );
		assertThat( node.getCollectionId() ).isEqualTo( "test-custom-id" );
	}

	@Test
	void testPrimaryKey() {
		IdDataNode node = new MockIdDataNode();
		assertThat( node.getPrimaryKey() ).containsExactly( IdDataNode.ID );
		assertThat( node.isPrimaryKey( IdDataNode.ID ) ).isTrue();
	}

}
