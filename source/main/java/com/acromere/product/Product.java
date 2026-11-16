package com.acromere.product;

import com.acromere.settings.Settings;

import java.nio.file.Path;

/**
 * The core interface for a running product.
 *
 * <p>A product is an installable or runnable unit of software that is
 * described by a {@link ProductCard} and that has its own settings and a
 * shared data folder. Implementations of this interface are the entry points
 * through which other products and tools access a product's metadata and
 * runtime resources.</p>
 *
 * <p>Products may form a hierarchy: a product can have a parent product, and
 * {@link #getParent()} returns it. This is used, for example, by
 * {@link Rb} to fall back to the parent's resource bundles when a value is
 * missing.</p>
 *
 * @see ProductCard
 * @see ProgramProduct
 * @see Program
 */
public interface Product {

	/**
	 * Get the product's {@link ProductCard}.
	 *
	 * @return The product's product card
	 */
	ProductCard getCard();

	/**
	 * Get the product settings.
	 *
	 * @return The product settings
	 */
	Settings getSettings();

	/**
	 * Get the shared product data folder. This is the location where the product
	 * should be able to store files that are specific to the product. This path
	 * is operating system specific and can be different between different
	 * versions of operating system.
	 * <p>
	 * Note: This folder is shared by multiple instances of the product.
	 *
	 * @return The product data folder.
	 */
	Path getDataFolder();

	/**
	 * Get the product parent.
	 *
	 * @return The parent Product or null if the product does not have a parent
	 */
	default Product getParent() {return null;}

}
