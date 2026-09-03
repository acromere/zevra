/**
 * A simple, yet powerful, general event system. Events for any kind of activity
 * can be defined and distributed with the classes in this package. Events are
 * defined by subclassing the {@link com.acromere.event.Event Event} class,
 * defining {@link com.acromere.event.EventType EventTypes} for that class,
 * and distributing them with the {@link com.acromere.event.EventHub EventHub}
 * class. {@link com.acromere.event.EventHandler Handlers} can be registered to
 * handle only the event types it can handle and use of lambdas as the handler
 * implementation is encouraged.
 */
package com.acromere.event;