package com.acromere.data;

import java.util.Comparator;
import java.util.List;

/**
 * A {@link Comparator} for {@link DataNode nodes}. The comparator is constructed
 * with the keys that should be compared.
 *
 * @param <T>  The node type
 * @param keys The value keys used to compare the nodes
 */
public record DataNodeComparator<T extends DataNode>(List<String> keys) implements Comparator<T> {

	/**
	 * Create a new node comparator for the specified value keys.
	 *
	 * @param keys The value keys used to compare the nodes
	 * @param <T>  The node type
	 * @return A new node comparator
	 */
	public static <T extends DataNode> DataNodeComparator<T> of( String... keys ) {
		return new DataNodeComparator<>( List.of( keys ) );
	}

	/**
	 * Create a new node comparator for the specified value keys.
	 *
	 * @param keys The value keys used to compare the nodes
	 * @param <T>  The node type
	 * @return A new node comparator
	 */
	public static <T extends DataNode> DataNodeComparator<T> of( List<String> keys ) {
		return new DataNodeComparator<>( keys );
	}

	/**
	 * Compare the two nodes by the value of each key in order. The comparison
	 * is performed using the string representation of each value. A node with
	 * a null value for a key is considered greater than a node with a non-null
	 * value for that key.
	 *
	 * @param a The first node
	 * @param b The second node
	 * @return A negative integer, zero, or a positive integer as the first node
	 * is less than, equal to, or greater than the second node
	 */
	@Override
	public int compare( T a, T b ) {
		for( String key : keys ) {
			Object valueA = a.getValue( key );
			Object valueB = b.getValue( key );
			if( valueA == null && valueB != null ) return 1;
			if( valueA != null && valueB == null ) return -1;

			int comparison = String.valueOf( valueA ).compareTo( String.valueOf( valueB ) );
			if( comparison != 0 ) return comparison;
		}
		return 0;
	}

}
