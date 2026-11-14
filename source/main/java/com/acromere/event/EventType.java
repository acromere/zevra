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

	public static final EventType<Event> ROOT = new EventType<>( null, "EVENT" );

	private final EventType<? super T> parent;

	@Getter
	private final String name;

	public EventType( final String name ) {
		this( ROOT, name );
	}

	public EventType( final EventType<? super T> parent ) {
		this( parent, parent.getName() );
	}

	public EventType( final EventType<? super T> parent, final String name ) {
		this.parent = parent;
		this.name = name;
	}

	public EventType<? super T> getParentEventType() {
		return parent;
	}

	@Override
	public String toString() {
		return name;
	}

}
