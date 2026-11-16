package com.acromere.skill;

/**
 * The Joinable interface provides an API for waiting on the completion of a
 * background operation.
 *
 * @param <T> the type of the result produced by the operation
 */
public interface Joinable<T> {

	/**
	 * Blocks until the operation completes.
	 *
	 * @return the result of the operation
	 */
	T join();

}
