package com.acromere.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTokenizerTest {

	@Test
	void testGetTokensSimple() {
		SimpleTokenizer tokenizer = new SimpleTokenizer( "alpha beta gamma" );
		List<String> tokens = tokenizer.getTokens();

		assertThat( tokens ).containsExactly( "alpha", "beta", "gamma" );
	}

	@Test
	void testGetTokensWithQuotes() {
		SimpleTokenizer tokenizer = new SimpleTokenizer( "command --option \"value with spaces\" final" );
		List<String> tokens = tokenizer.getTokens();

		assertThat( tokens ).containsExactly( "command", "--option", "value with spaces", "final" );
	}

	@Test
	void testGetTokensWithEscapedQuotes() {
		SimpleTokenizer tokenizer = new SimpleTokenizer( "arg1 \"quoted with \\\"escaped\\\" text\" arg2" );
		List<String> tokens = tokenizer.getTokens();

		assertThat( tokens ).containsExactly( "arg1", "quoted with \"escaped\" text", "arg2" );
	}

	@Test
	void testEmptyAndWhitespaceString() {
		assertThat( new SimpleTokenizer( "" ).getTokens() ).isEmpty();
		assertThat( new SimpleTokenizer( "   \t  \n  " ).getTokens() ).isEmpty();
	}

	@Test
	void testNextTokenDirectly() {
		SimpleTokenizer tokenizer = new SimpleTokenizer( "one two" );

		assertThat( tokenizer.nextToken() ).isEqualTo( "one" );
		assertThat( tokenizer.nextToken() ).isEqualTo( "two" );
		assertThat( tokenizer.nextToken() ).isNull();
		assertThat( tokenizer.nextToken() ).isNull();
	}

}
