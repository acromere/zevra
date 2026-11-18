package com.acromere.event;

import lombok.Getter;

/**
 * Represents a type of event within the event system. 
 * <p>
 * Event types can be organized in a hierarchy, where each event type can have 
 * a parent type. This allows for broad event handling where a handler can 
 * listen for a parent type and receive events of any of its child types.
 * </p>
 *
 * @param <T> The type of event associated with this event type.
 */
public final class EventType<T extends Event> {

	/**
	 * The root event type, which is the parent of all event types.
	 */
	public static final EventType<Event> ROOT = new EventType<>( null, "EVENT" );

	private final EventType<? super T> parent;

	/**
	 * The name of this event type.
	 */
	@Getter
	private final String name;

	/**
	 * Creates a new event type with the specified name,
	 * parented to the root event type.
	 *
	 * @param name The name of the event type
	 */
	public EventType( final String name ) {
		this( ROOT, name );
	}

	/**
	 * Creates a new event type with the specified parent and the
	 * same name as the parent.
	 *
	 * @param parent The parent event type
	 */
	public EventType( final EventType<? super T> parent ) {
		this( parent, parent.getName() );
	}

	/**
	 * Creates a new event type with the specified parent and name.
	 *
	 * @param parent The parent event type
	 * @param name The name of the event type
	 */
	public EventType( final EventType<? super T> parent, final String name ) {
		this.parent = parent;
		this.name = name;
	}

	/**
	 * Returns the parent of this event type.
	 *
	 * @return The parent event type, or null if this is the root event type
	 */
	public EventType<? super T> getParentEventType() {
		return parent;
	}

	/**
	 * Returns the name of this event type.
	 *
	 * @return The name of this event type
	 */
	@Override
	public String toString() {
		return name;
	}

}
