package com.acromere.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HashStrategyTest {

	@Test
	void testHashStrategyImplementation() {
		HashStrategy strategy = input -> HashUtil.hash( input, HashUtil.SHA1 );
		String hash = strategy.hash( new ByteArrayInputStream( "test".getBytes( StandardCharsets.UTF_8 ) ) );

		assertThat( hash ).isEqualTo( HashUtil.hash( "test", HashUtil.SHA1 ) );
	}

}
