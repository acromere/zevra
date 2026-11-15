package com.acromere.skill;

/**
 * A convenience interface for components that can run, be paused, and be
 * reset.
 *
 * <p>Combines {@link Runnable}, {@link Pausable}, and {@link Resettable} so
 * that a single component can be passed to callers that accept any of the
 * interfaces. This is typically used to drive run, pause, and reset user
 * interface actions for a single target.</p>
 *
 * @see Runnable
 * @see Pausable
 * @see Resettable
 */
public interface RunPauseResettable extends Runnable, Pausable, Resettable {}
