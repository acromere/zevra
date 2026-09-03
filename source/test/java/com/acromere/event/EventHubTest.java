package com.acromere.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

class EventHubTest {

	@Test
	void testHandle() {
		List<Event> rootEvents = new ArrayList<>();
		List<Event> testEvents1 = new ArrayList<>();
		List<Event> testEvents2 = new ArrayList<>();
		List<Event> aEvents = new ArrayList<>();
		List<Event> bEvents = new ArrayList<>();

		EventHub bus = new EventHub();
		bus.register( Event.ANY, rootEvents::add );
		bus.register( TestEvent.ANY, testEvents1::add );
		bus.register( TestEvent.ANY, testEvents2::add );
		bus.register( TestEvent.A, aEvents::add );
		bus.register( TestEvent.B, bEvents::add );

		bus.dispatch( new TestEvent( this, TestEvent.ANY ) );
		bus.dispatch( new TestEvent( this, TestEvent.A ) );
		bus.dispatch( new TestEvent( this, TestEvent.B ) );
		bus.dispatch( new Event( this, Event.ANY ) );

		assertThat( rootEvents.size() ).isEqualTo( 4 );
		assertThat( testEvents1.size() ).isEqualTo( 3 );
		assertThat( testEvents2.size() ).isEqualTo( 3 );
		assertThat( aEvents.size() ).isEqualTo( 1 );
		assertThat( bEvents.size() ).isEqualTo( 1 );
	}

	@Test
	void testPrior() {
		EventHub bus = new EventHub();
		assertThat( bus.<Event> getPriorEvent( TestEvent.class ) ).isNull();
		TestEvent any = new TestEvent( this, TestEvent.ANY );
		bus.dispatch( any );
		assertThat( bus.<Event> getPriorEvent( TestEvent.class ) ).isEqualTo( any );

		TestEvent a = new TestEvent( this, TestEvent.A );
		bus.dispatch( a );
		assertThat( bus.<Event> getPriorEvent( TestEvent.class ) ).isEqualTo( a );

		TestEvent b = new TestEvent( this, TestEvent.B );
		bus.dispatch( b );
		assertThat( bus.<Event> getPriorEvent( TestEvent.class ) ).isEqualTo( b );
	}

	@Test
	void testDispatchWithPeer() {
		List<Event> testEvents = new ArrayList<>();
		List<Event> peerEvents = new ArrayList<>();

		EventHub bus = new EventHub();
		EventHub peer = new EventHub();
		bus.register( peer );
		bus.register( TestEvent.ANY, testEvents::add );
		peer.register( TestEvent.ANY, peerEvents::add );

		bus.dispatch( new TestEvent( this, TestEvent.ANY ) );
		assertThat( peerEvents.size() ).isEqualTo( 1 );
		assertThat( testEvents.size() ).isEqualTo( 1 );
	}

	@Test
	void testRemovingEventHandlerFromItself() {
		EventHub bus = new EventHub();

		EventHandler<TestEvent> handler = e -> {};
		bus.register( Event.ANY, handler );
		bus.register( Event.ANY, e -> bus.unregister( Event.ANY, handler ) );

		try {
			bus.dispatch( new TestEvent( this, TestEvent.ANY ) );
		} catch( ConcurrentModificationException exception ) {
			fail( "EventBus.dispatch() not implemented in a way that prevents ConcurrentModificationException" );
		}
	}

	@Test
	void testWeaklyMappedEventHandler() {
		// given
		EventHub bus = new EventHub();
		List<Event> events = new ArrayList<>();

		Object owner = new Object();
		EventHandler<TestEvent> handler = events::add;
		bus.register( owner, Event.ANY, handler );
		assertThat( bus.getEventHandlers( Event.ANY ) ).isNotEmpty();
		assertThat( events ).isEmpty();

		// when
		// Set owner to null to allow for garbage collected
		owner = null;
		System.gc();

		// then
		assertThat( bus.getEventHandlers( Event.ANY ) ).isEmpty();
		assertThat( events ).isEmpty();
	}

	@Test
	void testParentHubDispatch() {
		List<Event> parentEvents = new ArrayList<>();
		List<Event> childEvents = new ArrayList<>();

		EventHub parentBus = new EventHub();
		EventHub childBus = new EventHub();

		parentBus.register( Event.ANY, parentEvents::add );
		childBus.register( Event.ANY, childEvents::add );

		childBus.parent( parentBus );
		assertThat( childBus.getParent() ).isSameAs( parentBus );

		childBus.dispatch( new TestEvent( this, TestEvent.ANY ) );
		assertThat( childEvents.size() ).isEqualTo( 1 );
		assertThat( parentEvents.size() ).isEqualTo( 1 );

		childBus.parent( null );
		assertThat( childBus.getParent() ).isNotSameAs( parentBus );
	}

	@Test
	void testUnregisterPeer() {
		List<Event> peerEvents = new ArrayList<>();

		EventHub bus = new EventHub();
		EventHub peer = new EventHub();
		bus.register( peer );
		peer.register( TestEvent.ANY, peerEvents::add );

		bus.dispatch( new TestEvent( this, TestEvent.ANY ) );
		assertThat( peerEvents.size() ).isEqualTo( 1 );

		bus.unregister( peer );
		bus.dispatch( new TestEvent( this, TestEvent.ANY ) );
		assertThat( peerEvents.size() ).isEqualTo( 1 );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	void testUnregisterHandler() {
		List<Event> events = new ArrayList<>();
		EventHandler<Event> handler = events::add;

		EventHub bus = new EventHub();
		bus.register( Event.ANY, handler );
		assertThat( (Collection<EventHandler<Event>>)bus.getEventHandlers( Event.ANY ) ).contains( handler );

		bus.dispatch( new Event( this, Event.ANY ) );
		assertThat( events.size() ).isEqualTo( 1 );

		bus.unregister( Event.ANY, handler );
		assertThat( (Collection<EventHandler<Event>>)bus.getEventHandlers( Event.ANY ) ).doesNotContain( handler );

		bus.dispatch( new Event( this, Event.ANY ) );
		assertThat( events.size() ).isEqualTo( 1 );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	void testUnregisterWithOwnerAndNonExistentCases() {
		EventHub bus = new EventHub();
		Object owner1 = new Object();
		Object owner2 = new Object();
		EventHandler<Event> handler1 = e -> {};
		EventHandler<Event> handler2 = e -> {};

		bus.register( owner1, Event.ANY, handler1 );
		bus.register( owner1, TestEvent.ANY, handler2 );

		// Unregister non-existent owner
		bus.unregister( owner2, Event.ANY, handler1 );

		// Unregister non-existent type for owner
		EventType<Event> nonExistentType = new EventType<>( "OTHER" );
		bus.unregister( owner1, nonExistentType, handler1 );

		// Unregister handler1
		bus.unregister( owner1, Event.ANY, handler1 );
		assertThat( bus.getEventHandlers( Event.ANY ) ).isEmpty();
		assertThat( (Collection<EventHandler<Event>>)bus.getEventHandlers( TestEvent.ANY ) ).contains( handler2 );

		// Unregister handler2 (emptying the owner map)
		bus.unregister( owner1, TestEvent.ANY, handler2 );
		assertThat( bus.getEventHandlers( TestEvent.ANY ) ).isEmpty();
	}

	@Test
	@SuppressWarnings( "unchecked" )
	void testGetEventHandlersMap() {
		EventHub bus = new EventHub();
		EventHandler<Event> h1 = e -> {};
		EventHandler<Event> h2 = e -> {};

		bus.register( Event.ANY, h1 );
		bus.register( TestEvent.ANY, h2 );

		var allHandlers = bus.getEventHandlers();
		assertThat( allHandlers ).containsKey( Event.ANY );
		assertThat( allHandlers ).containsKey( TestEvent.ANY );
		assertThat( (Collection<EventHandler<? extends Event>>)allHandlers.get( Event.ANY ) ).contains( h1 );
		assertThat( (Collection<EventHandler<? extends Event>>)allHandlers.get( TestEvent.ANY ) ).contains( h2 );
	}

	@Test
	void testHandlerThrowsRuntimeExceptionDoesNotBreakDispatch() {
		EventHub bus = new EventHub();
		List<Event> receivedEvents = new ArrayList<>();

		bus.register( Event.ANY, e -> {
			throw new RuntimeException( "Handler error" );
		} );
		bus.register( Event.ANY, receivedEvents::add );

		Event event = new Event( this, Event.ANY );
		Event result = bus.dispatch( event );

		assertThat( result ).isSameAs( event );
		assertThat( receivedEvents ).containsExactly( event );
	}

	private static class TestEvent extends Event {

		public static final EventType<TestEvent> TEST = new EventType<>( EventType.ROOT, "TEST" );

		public static final EventType<TestEvent> ANY = TEST;

		public static final EventType<TestEvent> A = new EventType<>( TestEvent.ANY, "A" );

		public static final EventType<TestEvent> B = new EventType<>( TestEvent.ANY, "B" );

		public TestEvent( Object source, EventType<TestEvent> type ) {
			super( source, type );
		}

	}

}
