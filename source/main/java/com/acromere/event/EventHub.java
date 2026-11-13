package com.acromere.event;

import lombok.CustomLog;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The central hub for managing and dispatching events.
 * <p>
 * The {@code EventHub} allows for the registration of handlers for specific 
 * {@link com.acromere.event.EventType}s and the dispatching of events to 
 * those handlers. It also supports a hierarchical structure where hubs can have 
 * a parent hub and peer hubs, allowing events to propagate across different 
 * parts of the system.
 * </p>
 * <p>
 * Events are dispatched to handlers of the event's type and all parent types.
 * </p>
 *
 * @see com.acromere.event.Event
 * @see com.acromere.event.EventType
 * @see com.acromere.event.EventHandler
 */
@CustomLog
public class EventHub {

	private static final EventHub ROOT = new EventHub();

	private final Set<EventHub> peers;

	private final Map<Object, Map<EventType<? extends Event>, Set<EventHandler<? extends Event>>>> eventTypeHandlers;

	private final Map<Class<? extends Event>, Event> priorEvent;

	private EventHub parent = ROOT;

	public EventHub() {
		this.peers = new CopyOnWriteArraySet<>();
		this.eventTypeHandlers = new WeakHashMap<>();
		this.priorEvent = new ConcurrentHashMap<>();
	}

	/**
	 * Dispatches the event to all registered handlers of the event's type 
	 * and all parent event types.
	 *
	 * @param event The event to dispatch
	 * @return The same event object
	 */
	public Event dispatch( Event event ) {
		// While the type of the incoming event is known, the parent event types
		// used later in the method are not well-known. They could be of any event
		// type, and therefore this variable needs to allow any event type.
		EventType<?> type = event.getEventType();

		priorEvent.put( event.getClass(), event );

		// Go through all the handlers of the event type and all handlers of all
		// the parent event types, passing the event to each handler.
		while( type != null ) {
			// If a ConcurrentModificationException occurs, try again until a clean
			// copy of the map can be generated.
			ConcurrentModificationException exception;
			do {
				try {
					exception = null;

					Collection<? extends EventHandler<Event>> typeHandlers = new HashSet<>( getEventHandlers( type ) );
					typeHandlers.forEach( v -> {
						try {
							v.handle( event );
						} catch( RuntimeException handlerException ) {
							// Do not let any handler break the others
							if( log.atConfig().isEnabled() ) {
								log.atConfig().withCause( handlerException ).log( "Event handler exception" );
							} else {
								log.atWarn().log( "Event handler exception: {0}", handlerException.getMessage() );
							}
						}
					} );
					type = type.getParentEventType();
				} catch( ConcurrentModificationException cme ) {
					exception = cme;
				}
			} while( exception != null );
		}

		// Dispatch the event to the peer hubs
		peers.forEach( p -> p.dispatch( event ) );

		// Dispatch the event to the parent hub
		if( this != ROOT ) parent.dispatch( event );

		return event;
	}

	/**
	 * Sets the parent hub for this hub. If no parent is provided, {@link #ROOT} is used.
	 *
	 * @param parent The parent hub
	 * @return This hub
	 */
	public EventHub parent( EventHub parent ) {
		this.parent = parent == null ? ROOT : parent;
		return this;
	}

	/**
	 * Registers a peer hub for this hub.
	 *
	 * @param peer The peer hub to register
	 */
	public void register( EventHub peer ) {
		this.peers.add( peer );
	}

	/**
	 * Unregisters a peer hub from this hub.
	 *
	 * @param peer The peer hub to unregister
	 */
	public void unregister( EventHub peer ) {
		this.peers.remove( peer );
	}

	/**
	 * Registers a handler for a specific event type.
	 *
	 * @param type The event type to register the handler for
	 * @param handler The handler to register
	 * @return This hub
	 */
	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return register( this, type, handler );
	}

	/**
	 * Registers a handler for a specific event type with a specific owner.
	 *
	 * @param owner  The owner of the registration
	 * @param type   The event type to register the handler for
	 * @param handler The handler to register
	 * @return This hub
	 */
	public <T extends Event> EventHub register( Object owner, EventType<? super T> type, EventHandler<? super T> handler ) {
		Map<EventType<? extends Event>, Set<EventHandler<? extends Event>>> typeHandlers = eventTypeHandlers.computeIfAbsent( owner, ( k ) -> new HashMap<>() );
		Set<EventHandler<? extends Event>> handlers = typeHandlers.computeIfAbsent( type, ( k ) -> new CopyOnWriteArraySet<>() );
		handlers.add( handler );
		return this;
	}

	/**
	 * Unregisters a handler for a specific event type.
	 *
	 * @param type The event type to unregister the handler from
	 * @param handler The handler to unregister
	 * @return This hub
	 */
	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return unregister( this, type, handler );
	}

	/**
	 * Unregisters a handler for a specific event type with a specific owner.
	 *
	 * @param owner  The owner of the registration
	 * @param type   The event type to unregister the handler from
 * @param handler   The handler to unregister
 * @return This hub
	 */
	public <T extends Event> EventHub unregister( Object owner, EventType<? super T> type, EventHandler<? super T> handler ) {
		Map<EventType<? extends Event>, Set<EventHandler<? extends Event>>> typeHandlers = eventTypeHandlers.get( owner );
		if( typeHandlers == null ) return this;
		Set<EventHandler<? extends Event>> handlers = typeHandlers.get( type );
		if( handlers == null ) return this;
		handlers.remove( handler );
		if( handlers.isEmpty() ) typeHandlers.remove( type );
		if( typeHandlers.isEmpty() ) eventTypeHandlers.remove( owner, typeHandlers );

		return this;
	}

	/**
	 * Returns a map of all event types to their associated collections of handlers.
	 *
	 * @return A map of event types to handler collections
	 */
	@SuppressWarnings( "unchecked" )
	public Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> result = new HashMap<>();

		eventTypeHandlers.values().forEach( v -> v.keySet().forEach( k -> {
			Collection<? extends EventHandler<? extends Event>> handlers = result.computeIfAbsent( k, y -> new CopyOnWriteArraySet<>() );
			((Collection<EventHandler<? extends Event>>)handlers).addAll( v.get( k ) );
		} ) );

		return result;
	}

	/**
	 * Returns the collection of handlers for a specific event type.
	 *
	 * @param type The event type
	 * @return A collection of handlers for the specified event type
	 */
	@SuppressWarnings( "unchecked" )
	public Collection<? extends EventHandler<Event>> getEventHandlers( EventType<? extends Event> type ) {
		Collection<EventHandler<Event>> result = new HashSet<>();
		eventTypeHandlers.values().forEach( m -> {
			Set<EventHandler<? extends Event>> handlers = m.getOrDefault( type, Set.of() );
			handlers.forEach( h -> result.add( (EventHandler<Event>)h ) );
		} );
		return result;
	}

	/**
	 * Returns the prior event of a specific class, if any.
	 *
	 * @param type The event class
	 * @return The prior event, or null if none exists
	 */
	@SuppressWarnings( "unchecked" )
	public <T extends Event> T getPriorEvent( Class<? extends Event> type ) {
		return (T)priorEvent.get( type );
	}

	/**
	 * Returns the parent hub of this hub.
	 *
	 * @return The parent hub
	 */
	protected EventHub getParent() {
		return parent;
	}

}
