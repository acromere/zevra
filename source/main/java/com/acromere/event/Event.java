package com.acromere.event;

import com.acromere.util.JavaUtil;

import java.util.EventObject;

/**
 * Base class for all events in the {@code com.acromere.event} system.
 * <p>
 * Events that go down the tree to children "cascade", while events that go up 
 * the tree to parents "bubble".
 * </p>
 * 
 * @see com.acromere.event.EventType
 */
public class Event extends EventObject {

	/**
	 * An event type that matches every event.
	 */
	public static final EventType<Event> ANY = EventType.ROOT;

	private final EventType<? extends Event> type;

	private final String toString;

	/**
	 * Creates a new event of a specific type.
	 *
	 * @param source The source object that the event originated from
	 * @param type The type of the event
	 */
	public Event( final Object source, final EventType<? extends Event> type ) {
		super( source );
		this.type = type;

		String sourceClass = JavaUtil.getClassName( source.getClass() );
		String eventClass = JavaUtil.getClassName( this.getClass() );
		toString = sourceClass + " > " + eventClass + " : " + getEventType().getName();
	}

	/**
	 * Returns the type of this event.
	 *
	 * @return The event type
	 */
	public EventType<? extends Event> getEventType() {
		return type;
	}

	/**
	 * Returns a string representation of this event.
	 *
	 * @return A string representation of this event
	 */
	public String toString() {
		return toString;
	}

}
