package com.acromere.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyEvalTest {

	@Test
	void testEvaluate() {
		LazyEval<String> lazy = () -> "computed value";
		assertThat( lazy.evaluate() ).isEqualTo( "computed value" );
	}

	@Test
	void testOf() {
		LazyEval<Integer> lazy = () -> 42;
		LazyEval<Integer> wrapped = LazyEval.of( lazy );
		assertThat( wrapped ).isSameAs( lazy );
		assertThat( wrapped.evaluate() ).isEqualTo( 42 );
	}

}
