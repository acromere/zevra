package com.acromere.product;

import com.acromere.util.IdGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Base class for all card types.
 *
 * <p>A card is a metadata descriptor for a product, repository, or catalog.
 * Every card is assigned a unique internal id when it is created, which is
 * used to identify the card within a session independent of its content.</p>
 *
 * @see ProductCard
 * @see RepoCard
 * @see CatalogCard
 */
@Getter
@Setter
@Accessors( chain = true )
public abstract class BaseCard {

	/**
	 * The unique id of this card, assigned when the card is created.
	 */
	private String internalId;

	protected BaseCard() {
		this.internalId = IdGenerator.getId();
	}

}
