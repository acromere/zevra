package com.acromere.product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseCardTest {

	private static class TestCard extends BaseCard {}

	@Test
	void testDefaultInternalId() {
		BaseCard card1 = new TestCard();
		BaseCard card2 = new TestCard();

		assertThat( card1.getInternalId() ).isNotNull();
		assertThat( card1.getInternalId() ).isNotEmpty();
		assertThat( card2.getInternalId() ).isNotNull();
		assertThat( card2.getInternalId() ).isNotEqualTo( card1.getInternalId() );
	}

	@Test
	void testSetInternalId() {
		BaseCard card = new TestCard();
		BaseCard returned = card.setInternalId( "custom-id" );

		assertThat( returned ).isSameAs( card );
		assertThat( card.getInternalId() ).isEqualTo( "custom-id" );
	}

}
