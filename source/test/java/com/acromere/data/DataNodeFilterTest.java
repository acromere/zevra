package com.acromere.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataNodeFilterTest {

	@Test
	void testFilterAccept() {
		DataNodeFilter<MockDataNode> filter = node -> node != null && "pass".equals( node.getValue( "status" ) );

		MockDataNode passingNode = new MockDataNode();
		passingNode.setValue( "status", "pass" );

		MockDataNode failingNode = new MockDataNode();
		failingNode.setValue( "status", "fail" );

		assertThat( filter.accept( passingNode ) ).isTrue();
		assertThat( filter.accept( failingNode ) ).isFalse();
		assertThat( filter.accept( null ) ).isFalse();
	}

}
