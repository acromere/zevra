package com.acromere.index;

import java.util.Comparator;

/**
 * Compares {@link Hit Hits} by their search points, in descending order, and
 * then by the document title, in ascending order.
 */
class HitSort implements Comparator<Hit> {

	/**
	 * Compare two hits.
	 *
	 * @param hit1 The first hit
	 * @param hit2 The second hit
	 * @return A negative integer, zero, or a positive integer as the first
	 *         hit should come before, at the same position as, or after the
	 *         second hit in the sorted order
	 */
	@Override
	public int compare( Hit hit1, Hit hit2 ) {

		return Comparator.comparingInt( o -> ((Hit)o).getPoints() ).reversed()
			//.thenComparingInt( o -> ((Hit)o).getPriority() )
			.thenComparing( ( o ) -> ((Hit)o).getDocument().title() ).compare( hit1, hit2 );
	}

}
