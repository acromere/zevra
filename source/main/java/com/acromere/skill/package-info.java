/**
 * Capability interfaces for identifying and controlling program components.
 *
 * <p>This package provides small, composable interfaces that describe what a
 * program component can do. Implementing these interfaces allows callers to
 * interact with components generically, without depending on their concrete
 * types. They are typically implemented by managers and services that start,
 * stop, or otherwise manage long-running components, as well as by objects
 * whose state is persisted by unique identifier.</p>
 *
 * <p>Key interfaces in this package include:</p>
 * <ul>
 *   <li>{@link com.acromere.skill.Actionable Actionable} - A functional
 *       interface for executing an action that may throw checked
 *       exceptions</li>
 *   <li>{@link com.acromere.skill.Controllable Controllable} - A start/stop
 *       lifecycle for long-running components</li>
 *   <li>{@link com.acromere.skill.Identity Identity} /
 *       {@link com.acromere.skill.WritableIdentity WritableIdentity} - A
 *       unique identifier for objects, with an optional writable variant</li>
 *   <li>{@link com.acromere.skill.Joinable Joinable} - Waiting for the
 *       completion of a background operation</li>
 *   <li>{@link com.acromere.skill.Pausable Pausable} - Pausing an in-progress
 *       operation</li>
 *   <li>{@link com.acromere.skill.Resettable Resettable} - Resetting a
 *       component to its initial state</li>
 *   <li>{@link com.acromere.skill.RunPauseResettable RunPauseResettable} - A
 *       convenience interface combining {@code Runnable}, {@code Pausable},
 *       and {@code Resettable}</li>
 * </ul>
 *
 * @see com.acromere.skill.Actionable
 * @see com.acromere.skill.Controllable
 * @see com.acromere.skill.Identity
 * @see com.acromere.skill.WritableIdentity
 */
package com.acromere.skill;
