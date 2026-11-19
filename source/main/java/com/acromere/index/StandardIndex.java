package com.acromere.index;

import lombok.CustomLog;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The standard {@link Index} implementation.
 *
 * <p>The index is a map of a word to the set of hits for the word. The map
 * is thread-safe and can be pushed to from multiple threads.</p>
 */
@CustomLog
public class StandardIndex implements Index {

	private final Map<String, Set<Hit>> index;

	/**
	 * Create a standard index.
	 */
	public StandardIndex() {
		this.index = new ConcurrentHashMap<>();
	}

	/**
	 * Get the dictionary of words in the index.
	 *
	 * @return A set of the words in the index
	 */
	@Override
	public Set<String> getDictionary() {
		return new HashSet<>( index.keySet() );
	}

	/**
	 * Get the hits for a word.
	 *
	 * @param word The word to get the hits for
	 * @return A set of the hits for the word
	 */
	@Override
	public Set<Hit> getHits( String word ) {
		return index.getOrDefault( word, Set.of() );
	}

	/**
	 * Add hits to the index.
	 *
	 * @param hits The hits to add
	 * @return This index
	 */
	@Override
	public Index push( Collection<Hit> hits ) {
		hits.forEach( h -> index.computeIfAbsent( h.getWord(), k -> new CopyOnWriteArraySet<>() ).add( h ) );
		return this;
	}

	@Override
	public int hashCode() {
		return index.hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof StandardIndex) ) return false;
		return index.equals( ((StandardIndex)object).index );
	}

}
