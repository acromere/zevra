package com.acromere.event;

import java.util.EventListener;

/**
 * A functional interface for handling events.
 * <p>
 * Implementations of this interface can be registered with an {@link com.acromere.event.EventHub}
 * to respond to specific event types.
 * </p>
 *
 * @param <T> The type of event that this handler handles.
 */
@FunctionalInterface
public interface EventHandler<T extends Event> extends EventListener {

	void handle( T event );

}
