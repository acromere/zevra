package com.acromere.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapts an {@link Iterator} to an {@link Enumeration}, allowing iterator-based
 * collections to be used where the legacy {@code Enumeration} API is required.
 *
 * @param <E> The type of elements iterated
 *
 * @see <a href="https://stackoverflow.com/questions/7085990/java-enumeration-from-setstring">Enumeration from a Set of Strings</a>
 */
public class IteratorEnumerator<E> implements Enumeration<E> {

	private final Iterator<E> iterator;

	/**
	 * Creates an enumeration backed by the given iterator.
	 *
	 * @param iterator The iterator to adapt, must not be {@code null}
	 */
	public IteratorEnumerator( Iterator<E> iterator ) {
		this.iterator = iterator;
	}

	/**
	 * Returns the next element from the underlying iterator.
	 *
	 * @return The next element
	 * @throws java.util.NoSuchElementException if there are no more elements
	 */
	public E nextElement() {
		return iterator.next();
	}

	/**
	 * Checks whether the underlying iterator has more elements.
	 *
	 * @return {@code true} if there are more elements, {@code false} otherwise
	 */
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

}