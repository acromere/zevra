package com.acromere.product;

import java.util.Comparator;

/**
 * A comparator for sorting {@link RepoCard} instances by a selected field.
 *
 * <p>Instances compare cards in natural order of the configured
 * {@link Field}. The {@link Field#NAME} field is the default and is used when
 * an unrecognized field is configured.</p>
 *
 * @see RepoCard
 */
public class RepoCardComparator implements Comparator<RepoCard> {

	private final Field field;

	/**
	 * Create a comparator that sorts by the specified field.
	 *
	 * @param field The field to sort by
	 */
	public RepoCardComparator( Field field ) {
		this.field = field;
	}

	/**
	 * Compare two cards by the configured field.
	 *
	 * @param card1 The first card
	 * @param card2 The second card
	 * @return A negative integer, zero, or a positive integer as the first
	 *     card is less than, equal to, or greater than the second card
	 */
	@Override
	public int compare( RepoCard card1, RepoCard card2 ) {
		if( field == Field.REPO ) return card1.getUrl().compareTo( card2.getUrl() );
		return card1.getName().compareTo( card2.getName() );
	}

	/**
	 * The fields a {@link RepoCard} can be compared by.
	 */
	public enum Field {
		/**
		 * Compare by the repository name.
		 */
		NAME,
		/**
		 * Compare by the repository url.
		 */
		REPO
	}

}
