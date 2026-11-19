package com.acromere.index;

import com.acromere.result.Result;

import java.util.List;

/**
 * Searches an {@link Index} for {@link Hit Hits}.
 *
 * <p>Implementations of this interface define how a search is performed. A
 * {@link DirectSearch} finds exact matches of the search terms, while a
 * {@link FuzzySearch} finds close matches using the Levenshtein distance
 * algorithm.</p>
 */
public interface Search {

	/**
	 * Search the index with the query.
	 *
	 * @param index The index to search
	 * @param query The search query
	 * @return The list of hits found by the search
	 */
	Result<List<Hit>> search( Index index, IndexQuery query );

}
