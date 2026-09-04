package com.acromere.product;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepoCardComparatorTest {

	@Test
	void testCompareName() {
		RepoCard card1 = new RepoCard();
		card1.setName( "Beta Repo" );
		card1.setUrl( "https://example.com/repoB" );

		RepoCard card2 = new RepoCard();
		card2.setName( "Alpha Repo" );
		card2.setUrl( "https://example.com/repoA" );

		RepoCard card3 = new RepoCard();
		card3.setName( "Beta Repo" );
		card3.setUrl( "https://example.com/repoC" );

		RepoCardComparator comparator = new RepoCardComparator( RepoCardComparator.Field.NAME );

		assertThat( comparator.compare( card1, card2 ) ).isGreaterThan( 0 );
		assertThat( comparator.compare( card2, card1 ) ).isLessThan( 0 );
		assertThat( comparator.compare( card1, card3 ) ).isEqualTo( 0 );

		List<RepoCard> list = new ArrayList<>( List.of( card1, card2, card3 ) );
		list.sort( comparator );
		assertThat( list.get( 0 ).getName() ).isEqualTo( "Alpha Repo" );
	}

	@Test
	void testCompareRepo() {
		RepoCard card1 = new RepoCard();
		card1.setName( "Z Repo" );
		card1.setUrl( "https://example.com/repoB" );

		RepoCard card2 = new RepoCard();
		card2.setName( "A Repo" );
		card2.setUrl( "https://example.com/repoA" );

		RepoCard card3 = new RepoCard();
		card3.setName( "M Repo" );
		card3.setUrl( "https://example.com/repoB" );

		RepoCardComparator comparator = new RepoCardComparator( RepoCardComparator.Field.REPO );

		assertThat( comparator.compare( card1, card2 ) ).isGreaterThan( 0 );
		assertThat( comparator.compare( card2, card1 ) ).isLessThan( 0 );
		assertThat( comparator.compare( card1, card3 ) ).isEqualTo( 0 );

		List<RepoCard> list = new ArrayList<>( List.of( card1, card2, card3 ) );
		list.sort( comparator );
		assertThat( list.get( 0 ).getUrl() ).isEqualTo( "https://example.com/repoA" );
	}

	@Test
	void testFieldEnum() {
		assertThat( RepoCardComparator.Field.values() ).containsExactly(
			RepoCardComparator.Field.NAME,
			RepoCardComparator.Field.REPO
		);
		assertThat( RepoCardComparator.Field.valueOf( "NAME" ) ).isEqualTo( RepoCardComparator.Field.NAME );
		assertThat( RepoCardComparator.Field.valueOf( "REPO" ) ).isEqualTo( RepoCardComparator.Field.REPO );
	}

}
