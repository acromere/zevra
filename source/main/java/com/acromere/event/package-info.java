/**
 * A simple, yet powerful, general event system. Events for any kind of activity
 * can be defined and distributed with the classes in this package. Events are
 * defined by subclassing the {@link com.acromere.event.Event Event} class,
 * defining {@link com.acromere.event.EventType EventTypes} for that class,
 * and distributing them with the {@link com.acromere.event.EventHub EventHub}
 * class. {@link com.acromere.event.EventHandler Handlers} can be registered to
 * handle only the event types it can handle and use of lambdas as the handler
 * implementation is encouraged.
 * <p>
 * Example:
 * <pre>
 * EventHub hub = new EventHub();
 * EventType&lt;MyEvent&gt; type = new EventType&lt;&gt;( "MY_EVENT" );
 * hub.register( type, event -&gt; {
 *     System.out.println( "Received event: " + event );
 * } );
 * hub.dispatch( new MyEvent( source, type ) );
 * </pre>
 */
package com.acromere.event;
