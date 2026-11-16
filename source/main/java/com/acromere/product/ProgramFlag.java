package com.acromere.product;

import com.acromere.util.LogFlag;

/**
 * Standard command-line flags for program products.
 *
 * <p>This interface defines the flags that program products understand,
 * extending {@link LogFlag} with product-specific flags. Flags are passed to
 * the program on the command line and control its behavior.</p>
 *
 * @see LogFlag
 */
public interface ProgramFlag extends LogFlag {

	/**
	 * The help flag indicates the program should display the program help.
	 */
	String HELP = "--help";

}
