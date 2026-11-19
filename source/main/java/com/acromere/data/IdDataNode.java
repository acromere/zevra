package com.acromere.data;

import java.util.UUID;

/**
 * An abstract base class for {@link DataNode} types that use a string UUID
 * as the primary key. Subclasses get a generated ID value in the
 * {@link #ID} key when they are created.
 */
public abstract class IdDataNode extends DataNode {

	/**
	 * The name of the value key used to store the ID.
	 */
	public static final String ID = "id";

	/**
	 * Create a new ID data node with a generated UUID ID value.
	 */
	public IdDataNode() {
		definePrimaryKey( ID );
		setId( UUID.randomUUID().toString() );
	}

	/**
	 * Get the ID value of the node.
	 *
	 * @return The ID value
	 */
	public String getId() {
		return getValue( ID );
	}

	/**
	 * Set the ID value of the node.
	 *
	 * @param id The new ID value
	 * @param <T> The node type
	 * @return The node
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T setId( String id ) {
		// NOTE Collection ID should not be part of a transaction
		setCollectionId( id );
		setValue( ID, id );
		return (T)this;
	}

}
