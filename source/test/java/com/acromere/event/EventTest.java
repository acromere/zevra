package com.acromere.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

	@Test
	void testConstructorAndGetters() {
		Object source = new Object();
		EventType<Event> type = new EventType<>( "TEST_TYPE" );

		Event event = new Event( source, type );

		assertThat( event.getSource() ).isSameAs( source );
		assertThat( event.getEventType() ).isEqualTo( type );
		assertThat( Event.ANY ).isSameAs( EventType.ROOT );
	}

	@Test
	void testToString() {
		String source = "source-object";
		EventType<Event> type = new EventType<>( "CUSTOM_EVENT" );

		Event event = new Event( source, type );

		assertThat( event.toString() ).isEqualTo( "String > Event : CUSTOM_EVENT" );
	}

	@Test
	void testToStringWithSubclass() {
		Object source = 42;
		EventType<CustomTestEvent> type = new EventType<>( "SUB_EVENT" );

		CustomTestEvent event = new CustomTestEvent( source, type );

		assertThat( event.toString() ).isEqualTo( "Integer > EventTest$CustomTestEvent : SUB_EVENT" );
	}

	private static class CustomTestEvent extends Event {

		public CustomTestEvent( Object source, EventType<? extends Event> type ) {
			super( source, type );
		}

	}

}
