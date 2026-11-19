package com.acromere.index;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A set of {@link Hit Hits} that can be searched.
 *
 * <p>The index maps a word to the set of hits for the word. The dictionary
 * of the index is the set of words in the index. The {@link StandardIndex}
 * class is the standard implementation of this interface.</p>
 */
public interface Index {

	/**
	 * The name of the default index.
	 */
	String DEFAULT = "default";

	/**
	 * Merge a collection of indexes into a single index.
	 *
	 * @param indexes The indexes to merge
	 * @return A new index containing the hits of all the indexes
	 */
	static Index merge( Collection<Index> indexes ) {
		return indexes.stream().reduce( new StandardIndex(), Index::merge );
	}

	/**
	 * Merge two indexes into a single index.
	 *
	 * @param a The first index to merge
	 * @param b The second index to merge
	 * @return A new index containing the hits of both indexes
	 */
	static StandardIndex merge( Index a, Index b ) {
		StandardIndex merged = new StandardIndex();
		merged.push( a.getHits() );
		merged.push( b.getHits() );
		return merged;
	}

	/**
	 * Get the dictionary of words in the index.
	 *
	 * @return A set of the words in the index
	 */
	Set<String> getDictionary();

	/**
	 * Get the hits for a word.
	 *
	 * @param word The word to get the hits for
	 * @return A set of the hits for the word
	 */
	Set<Hit> getHits( String word );

	/**
	 * Add hits to the index.
	 *
	 * @param hits The hits to add
	 * @return This index
	 */
	Index push( Collection<Hit> hits );

	/**
	 * Get all the hits in the index.
	 *
	 * @return A set of all the hits in the index
	 */
	default Set<Hit> getHits() {
		return getDictionary().stream().flatMap( t -> getHits( t ).stream() ).collect( Collectors.toSet() );
	}

}
