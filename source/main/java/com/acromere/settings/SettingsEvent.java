package com.acromere.settings;

import com.acromere.event.Event;
import com.acromere.event.EventType;
import lombok.Getter;

/**
 * An event for changes to a {@link Settings} node.
 * <p>
 * Events are dispatched to the settings node's event hub when a value is
 * changed ({@link #CHANGED}), and when a stored settings node is loaded
 * from its backing store ({@link #LOADED}) or saved to it ({@link #SAVED}).
 *
 * @see Settings
 * @see Event
 */
@Getter
public class SettingsEvent extends Event {

	/**
	 * The event type for all settings events.
	 */
	public static final EventType<SettingsEvent> SETTINGS = new EventType<>( Event.ANY, "SETTINGS" );

	/**
	 * An alias for {@link #SETTINGS}.
	 */
	public static final EventType<SettingsEvent> ANY = SETTINGS;

	/**
	 * The event type for a settings node being saved to its backing store.
	 */
	public static final EventType<SettingsEvent> SAVED = new EventType<>( SETTINGS, "SAVED" );

	/**
	 * The event type for a settings value being changed.
	 */
	public static final EventType<SettingsEvent> CHANGED = new EventType<>( SETTINGS, "CHANGED" );

	/**
	 * The event type for a settings node being loaded from its backing
	 * store.
	 */
	public static final EventType<SettingsEvent> LOADED = new EventType<>( SETTINGS, "LOADED" );

	/**
	 * The absolute path of the settings node the event originated from.
	 */
	private final String path;

	/**
	 * The value key of the changed value, or {@code null} if the event
	 * does not pertain to a specific value.
	 */
	private final String key;

	/**
	 * The value before the change, or {@code null} if the event does not
	 * pertain to a value change.
	 */
	private final Object oldValue;

	/**
	 * The value after the change, or {@code null} if the event does not
	 * pertain to a value change.
	 */
	private final Object newValue;

	/**
	 * Creates a new settings event for the given settings node.
	 *
	 * @param settings The settings node the event originated from
	 * @param type The type of the event
	 * @param path The absolute path of the settings node
	 */
	public SettingsEvent( Settings settings, EventType<SettingsEvent> type, String path ) {
		this( settings, type, path, null, null );
	}

	/**
	 * Creates a new settings event for a value of the given settings node.
	 *
	 * @param settings The settings node the event originated from
	 * @param type The type of the event
	 * @param path The absolute path of the settings node
	 * @param key The value key
	 * @param newValue The value after the change
	 */
	public SettingsEvent( Settings settings, EventType<SettingsEvent> type, String path, String key, Object newValue ) {
		this( settings, type, path, key, null, newValue );
	}

	/**
	 * Creates a new settings event for a value of the given settings node.
	 *
	 * @param settings The settings node the event originated from
	 * @param type The type of the event
	 * @param path The absolute path of the settings node
	 * @param key The value key
	 * @param oldValue The value before the change
	 * @param newValue The value after the change
	 */
	public SettingsEvent( Settings settings, EventType<SettingsEvent> type, String path, String key, Object oldValue, Object newValue ) {
		super( settings, type );
		this.path = path;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * Get the settings node the event originated from.
	 *
	 * @return The settings node
	 */
	public Settings getSettings() {
		return (Settings)getSource();
	}

	/**
	 * Get the type of this event.
	 *
	 * @return The event type
	 */
	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<SettingsEvent> getEventType() {
		return (EventType<SettingsEvent>)super.getEventType();
	}

	/**
	 * Get a string representation of this event, including the settings
	 * path, the value key, and, for {@link #CHANGED} events, the old and
	 * new values.
	 *
	 * @return A string representation of this event
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( super.toString() );
		if( path != null ) builder.append( " path=" ).append( path );
		if( key != null ) builder.append( " key=" ).append( key );
		if( getEventType() == CHANGED ) builder.append( " old=" ).append( oldValue ).append( " new=" ).append( newValue );
		return builder.toString();
	}

}
