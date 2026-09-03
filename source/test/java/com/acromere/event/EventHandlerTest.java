package com.acromere.event;

import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class EventHandlerTest {

	@Test
	void testHandle() {
		AtomicReference<Event> handledEvent = new AtomicReference<>();
		EventHandler<Event> handler = handledEvent::set;

		Event event = new Event( this, Event.ANY );
		handler.handle( event );

		assertThat( handledEvent.get() ).isSameAs( event );
	}

	@Test
	void testHandleSubclassEvent() {
		AtomicReference<CustomSubEvent> handledEvent = new AtomicReference<>();
		EventHandler<CustomSubEvent> handler = handledEvent::set;

		EventType<CustomSubEvent> subType = new EventType<>( "SUB" );
		CustomSubEvent event = new CustomSubEvent( this, subType, "payload-data" );
		handler.handle( event );

		assertThat( handledEvent.get() ).isSameAs( event );
		assertThat( handledEvent.get().getData() ).isEqualTo( "payload-data" );
	}

	@Getter
	private static class CustomSubEvent extends Event {

		private final String data;

		public CustomSubEvent( Object source, EventType<? extends Event> type, String data ) {
			super( source, type );
			this.data = data;
		}

	}

}
