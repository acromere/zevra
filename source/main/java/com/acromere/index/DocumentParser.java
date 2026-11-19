package com.acromere.index;

import com.acromere.result.Result;

import java.util.Set;

/**
 * Parses a {@link Document} and indexes it into a set of {@link Hit Hits}.
 */
public interface DocumentParser {

	/**
	 * Index the document and return the hits found.
	 *
	 * @param document The document to index
	 * @return The set of hits found in the document
	 */
	Result<Set<Hit>> index( Document document );

}
