package com.acromere.skill;

/**
 * The Controllable interface provides an API for classes that are "controlled"
 * by {@link #start()} and {@link #stop()} methods.
 *
 * <p>Implementations of this interface are expected to be synchronous. This
 * means that when execution exits the {@link #start()} method the Controllable
 * is started and the {@link #isRunning()} method will return {@code true}.
 * Similarly, when execution exits the {@link #stop()} method the Controllable
 * is stopped and the {@link #isRunning()} method will return {@code false}.</p>
 *
 * <p>Whether {@link #start()} can be called again after {@link #stop()} has
 * been called is not defined by this interface. Implementations that support
 * repeated start/stop cycles should document that behavior separately.</p>
 *
 * @param <T> the type of the controllable, typically the implementing class
 *     itself
 */
@SuppressWarnings( { "unused", "UnusedReturnValue" } )
public interface Controllable<T> {

	/**
	 * Returns whether this Controllable is currently running.
	 *
	 * @return {@code true} if this Controllable is running, otherwise
	 *     {@code false}
	 */
	boolean isRunning();

	/**
	 * Starts this Controllable. Execution does not return until the
	 * Controllable has started, at which point {@link #isRunning()} returns
	 * {@code true}.
	 *
	 * @return the started Controllable
	 */
	T start();

	/**
	 * Stops this Controllable. Execution does not return until the
	 * Controllable has stopped, at which point {@link #isRunning()} returns
	 * {@code false}.
	 *
	 * @return the stopped Controllable
	 */
	T stop();

}
