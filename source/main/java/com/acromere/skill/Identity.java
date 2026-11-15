package com.acromere.skill;

/**
 * The Identity interface provides a unique identifier (UID) for objects that
 * implement it.
 *
 * <p>The UID is typically generated once and used to persist and restore the
 * state of an object across sessions. Implementations that need to assign or
 * change the UID should implement {@link WritableIdentity} instead.</p>
 */
public interface Identity {

	/**
	 * The key under which the UID is typically stored when the state of an
	 * object is persisted.
	 *
	 * <p>NOTE: Do not change the value of this key without a plan to migrate
	 * persisted values.</p>
	 */
	String KEY = "id";

	/**
	 * Returns the unique identifier of this object.
	 *
	 * @return the unique identifier of this object
	 */
	String getUid();

}
