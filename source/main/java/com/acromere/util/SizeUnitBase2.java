package com.acromere.util;

import lombok.Getter;

/**
 * Enumeration of size units based on powers of 2.
 *
 * <p>Each constant represents a binary size unit (e.g. {@link #KiB KiB} =
 * 1,024 bytes) together with a compact one-letter prefix used for
 * human-readable display (e.g. {@code "K"}).</p>
 *
 * @see SizeUnitBase10
 */
@Getter
public enum SizeUnitBase2 {

	/** Byte (1 byte). */
	B( 1, "B" ),
	/** Kibibyte (1,024 bytes). */
	KiB( 1024, "K" ),
	/** Mebibyte (1,048,576 bytes). */
	MiB( KiB.getSize() * 1024, "M" ),
	/** Gibibyte (1,073,741,824 bytes). */
	GiB( MiB.getSize() * 1024, "G" ),
	/** Tebibyte (2<sup>40</sup> bytes). */
	TiB( GiB.getSize() * 1024, "T" ),
	/** Pebibyte (2<sup>50</sup> bytes). */
	PiB( TiB.getSize() * 1024, "P" ),
	/** Exbibyte (2<sup>60</sup> bytes). */
	EiB( PiB.getSize() * 1024, "E" );

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
	SizeUnitBase2( long size, String compact ) {
		this.size = size;
		this.compact = compact;
	}

}
