package com.acromere.data;

import org.junit.jupiter.api.BeforeEach;

public abstract class BaseDataNodeTest {

	protected MockDataNode data;

	@BeforeEach
	void setup() {
		data = new MockDataNode();
	}

}
