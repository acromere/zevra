package com.acromere.util;

import lombok.Getter;

/**
 * Enumeration of size units based on powers of 10.
 *
 * <p>Each constant represents a decimal size unit (e.g. {@link #KB KB} =
 * 1,000 bytes) together with a compact one-letter prefix used for
 * human-readable display (e.g. {@code "K"}).</p>
 *
 * @see SizeUnitBase2
 */
@Getter
public enum SizeUnitBase10 {

	/** Byte (1 byte). */
	B( 1, "B" ),
	/** Kilobyte (1,000 bytes). */
	KB( 1000, "K" ),
	/** Megabyte (1,000,000 bytes). */
	MB( KB.getSize() * 1000, "M" ),
	/** Gigabyte (1,000,000,000 bytes). */
	GB( MB.getSize() * 1000, "G" ),
	/** Terabyte (10<sup>12</sup> bytes). */
	TB( GB.getSize() * 1000, "T" ),
	/** Petabyte (10<sup>15</sup> bytes). */
	PB( TB.getSize() * 1000, "P" ),
	/** Exabyte (10<sup>18</sup> bytes). */
	EB( PB.getSize() * 1000, "E" );

	/** The number of bytes this unit represents. */
	final long size;

	/** Compact one-letter prefix used for human-readable display. */
	final String compact;

	/**
	 * Creates a size unit.
	 *
	 * @param size The number of bytes this unit represents
	 * @param compact Compact one-letter prefix used for display
	 */
	SizeUnitBase10( long size, String compact ) {
		this.size = size;
		this.compact = compact;
	}

}
