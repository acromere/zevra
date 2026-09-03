package com.acromere.event;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventWatcherTest {

	@Test
	void testDefaultConstructor() {
		EventWatcher watcher = new EventWatcher();
		assertThat( watcher.getTimeout() ).isEqualTo( EventWatcher.DEFAULT_WAIT_TIMEOUT );
		assertThat( watcher.isPrintEventCapture() ).isFalse();
	}

	@Test
	void testCustomConstructorAndPrintEventCapture() {
		EventWatcher watcher = new EventWatcher( 1234 );
		assertThat( watcher.getTimeout() ).isEqualTo( 1234 );

		watcher.setPrintEventCapture( true );
		assertThat( watcher.isPrintEventCapture() ).isTrue();

		EventType<Event> type = new EventType<>( "TEST" );
		Event event = new Event( this, type );
		watcher.handle( event );

		watcher.setPrintEventCapture( false );
		assertThat( watcher.isPrintEventCapture() ).isFalse();
	}

	@Test
	void testWaitForEventWhenAlreadyPresent() throws Exception {
		EventWatcher watcher = new EventWatcher();
		EventType<Event> type = new EventType<>( "TEST" );
		Event event = new Event( this, type );

		watcher.handle( event );
		watcher.waitForEvent( type );
	}

	@Test
	void testWaitForEventWithExplicitTimeoutWhenAlreadyPresent() throws Exception {
		EventWatcher watcher = new EventWatcher();
		EventType<Event> type = new EventType<>( "TEST" );
		Event event = new Event( this, type );

		watcher.handle( event );
		watcher.waitForEvent( type, 500 );
	}

	@Test
	void testWaitForEventAsynchronous() throws Exception {
		EventWatcher watcher = new EventWatcher( 2000 );
		EventType<Event> type = new EventType<>( "TEST" );
		Event event = new Event( this, type );

		try( ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor() ) {
			executor.schedule( () -> watcher.handle( event ), 50, TimeUnit.MILLISECONDS );
			watcher.waitForEvent( type );
		}
	}

	@Test
	void testWaitForEventTimeout() {
		EventWatcher watcher = new EventWatcher( 50 );
		EventType<Event> type = new EventType<>( "TEST" );

		assertThatThrownBy( () -> watcher.waitForEvent( type ) )
			.isInstanceOf( TimeoutException.class )
			.hasMessageContaining( "Timeout waiting for event" )
			.hasMessageContaining( "TEST" );
	}

	@Test
	void testWaitForEventTimeoutWithExplicitTimeout() {
		EventWatcher watcher = new EventWatcher();
		EventType<Event> type = new EventType<>( "TEST" );

		assertThatThrownBy( () -> watcher.waitForEvent( type, 50 ) )
			.isInstanceOf( TimeoutException.class )
			.hasMessageContaining( "Timeout waiting for event" )
			.hasMessageContaining( "TEST" );
	}

	@Test
	void testWaitForNextEvent() throws Exception {
		EventWatcher watcher = new EventWatcher( 2000 );
		EventType<Event> type = new EventType<>( "TEST" );
		Event event1 = new Event( this, type );
		Event event2 = new Event( this, type );

		watcher.handle( event1 );

		try( ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor() ) {
			executor.schedule( () -> watcher.handle( event2 ), 50, TimeUnit.MILLISECONDS );
			watcher.waitForNextEvent( type );
		}
	}

	@Test
	void testWaitForNextEventWithExplicitTimeout() throws Exception {
		EventWatcher watcher = new EventWatcher();
		EventType<Event> type = new EventType<>( "TEST" );
		Event event1 = new Event( this, type );
		Event event2 = new Event( this, type );

		watcher.handle( event1 );

		try( ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor() ) {
			executor.schedule( () -> watcher.handle( event2 ), 50, TimeUnit.MILLISECONDS );
			watcher.waitForNextEvent( type, 2000 );
		}
	}

	@Test
	void testWaitForNextEventTimeout() {
		EventWatcher watcher = new EventWatcher();
		EventType<Event> type = new EventType<>( "TEST" );
		Event event1 = new Event( this, type );

		watcher.handle( event1 );

		assertThatThrownBy( () -> watcher.waitForNextEvent( type, 50 ) )
			.isInstanceOf( TimeoutException.class )
			.hasMessageContaining( "Timeout waiting for event" );
	}

	@Test
	void testWaitForEventIgnoresOtherEventTypes() {
		EventWatcher watcher = new EventWatcher();
		EventType<Event> typeA = new EventType<>( "TYPE_A" );
		EventType<Event> typeB = new EventType<>( "TYPE_B" );
		Event eventA = new Event( this, typeA );

		watcher.handle( eventA );

		assertThatThrownBy( () -> watcher.waitForEvent( typeB, 50 ) )
			.isInstanceOf( TimeoutException.class );
	}

}
