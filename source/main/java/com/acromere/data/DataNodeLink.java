package com.acromere.data;

import lombok.Getter;

/**
 * A data node that holds a reference to another data node. The link is itself
 * an {@link IdDataNode}, so it can be used as a value in a
 * {@link DataNode#getValue(String) node value} without directly exposing the
 * linked node. Use the static {@link #of(DataNode)} method to create a new
 * link.
 *
 * @param <T> The type of the linked {@link DataNode}
 */
@Getter
public class DataNodeLink<T extends DataNode> extends IdDataNode {

	/**
	 * The node the link points to.
	 */
	private final T node;

	/**
	 * Create a new data node link to the specified node.
	 *
	 * @param node The node the link points to
	 */
	public DataNodeLink( T node ) {
		this.node = node;
	}

	/**
	 * Create a new data node link to the specified node.
	 *
	 * @param node The node the link points to
	 * @param <T>  The type of the linked node
	 * @return A new data node link
	 */
	public static <T extends DataNode> DataNodeLink<T> of( T node ) {
		return new DataNodeLink<>( node );
	}

	/**
	 * Get a string representation of this link.
	 *
	 * @return A string representation of this link
	 */
	@Override
	public String toString() {
		return "NodeLink@" + node;
	}

}
