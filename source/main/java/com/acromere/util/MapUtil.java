package com.acromere.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class for map operations.
 */
public class MapUtil {

	/**
	 * Returns a new map with keys and values of the given map swapped.
	 *
	 * <p>If the original map contains duplicate values, only the last key
	 * associated with each value is retained in the mirrored map.</p>
	 *
	 * @param <K> The key type of the original map
	 * @param <V> The value type of the original map
	 * @param map The map to mirror
	 * @return A new map where each original value maps to its original key
	 */
	public static <K, V> Map<V, K> mirror( Map<K, V> map ) {
		Map<V, K> mirror = new HashMap<>();

		for( K key : map.keySet() ) {
			mirror.put( map.get( key ), key );
		}

		return mirror;
	}

	/**
	 * Flattens a tree-structured map into a stream of its leaf values.
	 *
	 * <p>The map is expected to model a tree where each node is a map that
	 * may contain a value under {@code valueKey} and child nodes under
	 * {@code treeKey}. Values are emitted in a pre-order traversal (parent
	 * value, then children).</p>
	 *
	 * @param <K> The key type of the maps
	 * @param <V> The value type of the leaf values
	 * @param map The root map of the tree
	 * @param treeKey The key under which child nodes are stored
	 * @param valueKey The key under which a node's value is stored
	 * @return A stream of all values found in the tree
	 */
	@SuppressWarnings( { "unchecked" } )
	public static <K, V> Stream<V> flatten( Map<K, ?> map, K treeKey, K valueKey ) {
		return MapUtil.flatTreeMap(
			map, m -> {
				Map<K, ?> children = (Map<K, ?>)m.get( treeKey );
				return children == null ? Map.of() : children;
			}, m -> {
				V value = (V)m.get( valueKey );
				return value == null ? Stream.of() : Stream.of( value );
			}
		);
	}

	/**
	 * Recursively flattens a tree of maps into a stream of values, using the
	 * given functions to locate children and extract values from each node.
	 *
	 * <p>This is the general form of {@link #flatten(Map, Object, Object)
	 * flatten()}. Values are emitted in a pre-order traversal: a node's own
	 * value first, followed by the values of its children.</p>
	 *
	 * @param <K> The key type of the maps
	 * @param <V> The value type extracted from nodes
	 * @param map The root map of the tree
	 * @param flattener Function that returns the child nodes of a given node
	 * (as a map), or an empty map if the node has no children
	 * @param extractor Function that returns the value(s) stored in a given
	 * node, or an empty stream if the node has no value
	 * @return A stream of all values found in the tree
	 */
	@SuppressWarnings( "unchecked" )
	public static <K, V> Stream<V> flatTreeMap( Map<K, ?> map, Function<Map<K, ?>, Map<K, ?>> flattener, Function<Map<K, ?>, Stream<V>> extractor ) {
		return Stream.concat( extractor.apply( map ), flattener.apply( map ).values().stream().flatMap( v -> flatTreeMap( (Map<K, ?>)v, flattener, extractor ) ) );
	}

}
