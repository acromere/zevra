package com.acromere.product;

import java.util.Comparator;

/**
 * A comparator for sorting {@link ProductCard} instances by a selected field.
 *
 * <p>Instances compare cards in natural order of the configured
 * {@link Field}. The {@link Field#NAME} field is the default and is used when
 * an unrecognized field is configured.</p>
 *
 * @see ProductCard
 */
public class ProductCardComparator implements Comparator<ProductCard> {

	private final Field field;

	/**
	 * Create a comparator that sorts by the specified field.
	 *
	 * @param field The field to sort by
	 */
	public ProductCardComparator( Field field ) {
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
	public int compare( ProductCard card1, ProductCard card2 ) {
		switch( field ) {
			case KEY: {
				return card1.getProductKey().compareTo( card2.getProductKey() );
			}
			case RELEASE: {
				return card1.getRelease().compareTo( card2.getRelease() );
			}
			case ARTIFACT: {
				return card1.getArtifact().compareTo( card2.getArtifact() );
			}
			case GROUP: {
				return card1.getGroup().compareTo( card2.getGroup() );
			}
			case VERSION: {
				return card1.getVersion().compareTo( card2.getVersion() );
			}
			default: {
				return card1.getName().compareTo( card2.getName() );
			}
		}
	}

	/**
	 * The fields a {@link ProductCard} can be compared by.
	 */
	public enum Field {
		/**
		 * Compare by the product key.
		 */
		KEY,
		/**
		 * Compare by the product name.
		 */
		NAME,
		/**
		 * Compare by the product group.
		 */
		GROUP,
		/**
		 * Compare by the product artifact.
		 */
		ARTIFACT,
		/**
		 * Compare by the product release.
		 */
		RELEASE,
		/**
		 * Compare by the product version.
		 */
		VERSION
	}

}
