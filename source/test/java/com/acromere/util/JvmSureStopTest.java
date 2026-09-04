package com.acromere.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class JvmSureStopTest {

	@Test
	void testConstructors() {
		JvmSureStop stopDefault = new JvmSureStop();
		assertThat( stopDefault.getName() ).isEqualTo( "JVM Sure Stop" );
		assertThat( stopDefault.isDaemon() ).isTrue();

		JvmSureStop stopCustom = new JvmSureStop( 5000 );
		assertThat( stopCustom.getName() ).isEqualTo( "JVM Sure Stop" );
		assertThat( stopCustom.isDaemon() ).isTrue();
	}

	@Test
	void testRunDuringUnitTest() {
		JvmSureStop stop = new JvmSureStop( 100 );
		assertThatCode( stop::run ).doesNotThrowAnyException();
	}

}
