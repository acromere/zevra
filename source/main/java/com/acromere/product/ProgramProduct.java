package com.acromere.product;

/**
 * A product that represents an executable program.
 *
 * <p>This interface narrows {@link Product} to products that are programs,
 * providing access to the running program instance.</p>
 *
 * @see Product
 * @see Program
 */
public interface ProgramProduct extends Product {

	/**
	 * Get the running program.
	 *
	 * @return The running program
	 */
	Program getProgram();

}
