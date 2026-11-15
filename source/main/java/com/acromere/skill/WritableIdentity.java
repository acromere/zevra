package com.acromere.skill;

/**
 * The WritableIdentity interface extends {@link Identity} with a method to
 * set the unique identifier of an object.
 */
public interface WritableIdentity extends Identity {

	/**
	 * Sets the unique identifier of this object.
	 *
	 * @param uid the unique identifier to set
	 */
	void setUid( String uid );

}
