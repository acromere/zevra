/**
 * A hierarchical settings and configuration management system.
 * <p>
 * The core interface is {@link com.acromere.settings.Settings Settings}, which
 * represents a node in a hierarchical tree of settings identified by path names.
 * Settings support storing and retrieving values with automatic conversion for
 * simple types, Java beans, arrays, and collections.
 * <p>
 * Implementations include:
 * <ul>
 *   <li>{@link com.acromere.settings.AbstractSettings AbstractSettings} - Base implementation providing type conversion, default value resolution, and event handling</li>
 *   <li>{@link com.acromere.settings.MapSettings MapSettings} - In-memory implementation backed by concurrent maps</li>
 *   <li>{@link com.acromere.settings.StoredSettings StoredSettings} - Persistent implementation backed by file system properties with asynchronous saving</li>
 * </ul>
 * <p>
 * Changes to settings can be observed using {@link com.acromere.settings.SettingsEvent SettingsEvents}.
 */
package com.acromere.settings;
