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

}
