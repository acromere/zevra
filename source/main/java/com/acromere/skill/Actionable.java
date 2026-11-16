package com.acromere.skill;

/**
 * A functional interface for executing an action that may throw checked
 * exceptions.
 *
 * <p>Just like {@link Runnable} except that managed exceptions are allowed.
 * This makes the interface suitable for wrapping code that performs fallible
 * operations, such as I/O, where the caller is expected to handle or
 * propagate failures.</p>
 *
 * @see Runnable
 */
@FunctionalInterface
public interface Actionable {

	/**
	 * Performs the action.
	 *
	 * @throws Exception if the action fails
	 */
	void act() throws Exception;

}
