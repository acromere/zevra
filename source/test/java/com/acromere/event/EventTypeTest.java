package com.acromere.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTypeTest {

	@Test
	void testRootEventType() {
		EventType<Event> root = EventType.ROOT;
		assertThat( root.getParentEventType() ).isNull();
		assertThat( root.getName() ).isEqualTo( "EVENT" );
		assertThat( root.toString() ).isEqualTo( "EVENT" );
	}

	@Test
	void testConstructorWithNameOnly() {
		EventType<Event> type = new EventType<>( "CUSTOM" );
		assertThat( type.getParentEventType() ).isSameAs( EventType.ROOT );
		assertThat( type.getName() ).isEqualTo( "CUSTOM" );
		assertThat( type.toString() ).isEqualTo( "CUSTOM" );
	}

	@Test
	void testConstructorWithParentOnly() {
		EventType<Event> parent = new EventType<>( "PARENT" );
		EventType<Event> child = new EventType<>( parent );

		assertThat( child.getParentEventType() ).isSameAs( parent );
		assertThat( child.getName() ).isEqualTo( "PARENT" );
		assertThat( child.toString() ).isEqualTo( "PARENT" );
	}

	@Test
	void testConstructorWithParentAndName() {
		EventType<Event> parent = new EventType<>( "PARENT" );
		EventType<Event> child = new EventType<>( parent, "CHILD" );

		assertThat( child.getParentEventType() ).isSameAs( parent );
		assertThat( child.getName() ).isEqualTo( "CHILD" );
		assertThat( child.toString() ).isEqualTo( "CHILD" );
	}

	@Test
	void testHierarchy() {
		EventType<Event> level1 = new EventType<>( EventType.ROOT, "L1" );
		EventType<Event> level2 = new EventType<>( level1, "L2" );
		EventType<Event> level3 = new EventType<>( level2, "L3" );

		assertThat( level3.getParentEventType() ).isSameAs( level2 );
		assertThat( level3.getParentEventType().getParentEventType() ).isSameAs( level1 );
		assertThat( level3.getParentEventType().getParentEventType().getParentEventType() ).isSameAs( EventType.ROOT );
		assertThat( level3.getParentEventType().getParentEventType().getParentEventType().getParentEventType() ).isNull();
	}

}
