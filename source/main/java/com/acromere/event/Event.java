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

	public static final EventType<Event> ANY = EventType.ROOT;

	private final EventType<? extends Event> type;

	private final String toString;

	public Event( final Object source, final EventType<? extends Event> type ) {
		super( source );
		this.type = type;

		String sourceClass = JavaUtil.getClassName( source.getClass() );
		String eventClass = JavaUtil.getClassName( this.getClass() );
		toString = sourceClass + " > " + eventClass + " : " + getEventType().getName();
	}

	public EventType<? extends Event> getEventType() {
		return type;
	}

	public String toString() {
		return toString;
	}

}
