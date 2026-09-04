package com.acromere.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JvmTest {

	@Test
	void testJvmId() {
		assertThat( Jvm.ID ).isGreaterThanOrEqualTo( 0L );
		assertThat( new Jvm() ).isNotNull();
	}

}
