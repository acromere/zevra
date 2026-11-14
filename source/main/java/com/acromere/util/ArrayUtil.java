package com.acromere.util;

import java.util.Arrays;

/**
 * Utility class for array operations.
 */
public class ArrayUtil {

	/**
	 * Returns a new array containing the elements of the source array
	 * followed by the additional elements.
	 *
	 * @param <T> The component type of the arrays
	 * @param source The array to append to, must not be {@code null}
	 * @param other The elements to append
	 * @return A new array containing all elements in order
	 */
	@SafeVarargs
	public static <T> T[] append( T[] source, T... other ) {
		return concat( source, other );
	}

	/**
	 * Concatenates the source array with the additional elements, returning a
	 * new array. The source array is not modified.
	 *
	 * @param <T> The component type of the arrays
	 * @param source The array to concatenate from, must not be {@code null}
	 * @param other The elements to concatenate
	 * @return A new array containing all elements in order
	 */
	@SafeVarargs
	public static <T> T[] concat( T[] source, T... other ) {
		T[] concat = Arrays.copyOf( source, source.length + other.length );
		System.arraycopy( other, 0, concat, source.length, other.length );
		return concat;
	}

}
