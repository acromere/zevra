package com.acromere.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PersonTest {

	@Test
	void testGettersAndSetters() {
		Person person = new Person();
		person.setName( "Jane Doe" );
		person.setEmail( "jane@example.com" );
		person.setTimezone( "America/Denver" );

		assertThat( person.getName() ).isEqualTo( "Jane Doe" );
		assertThat( person.getEmail() ).isEqualTo( "jane@example.com" );
		assertThat( person.getTimezone() ).isEqualTo( "America/Denver" );
		assertThat( person.toString() ).isEqualTo( "Jane Doe" );
	}

	@Test
	void testJsonSerialization() throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		Person person = new Person();
		person.setName( "John Smith" );
		person.setEmail( "john@example.com" );
		person.setTimezone( "UTC" );

		String json = mapper.writeValueAsString( person );
		assertThat( json ).contains( "\"name\":\"John Smith\"" );
		assertThat( json ).contains( "\"email\":\"john@example.com\"" );
		assertThat( json ).contains( "\"timezone\":\"UTC\"" );

		Person deserialized = mapper.readValue( json, Person.class );
		assertThat( deserialized.getName() ).isEqualTo( "John Smith" );
		assertThat( deserialized.getEmail() ).isEqualTo( "john@example.com" );
		assertThat( deserialized.getTimezone() ).isEqualTo( "UTC" );
	}

	@Test
	void testJsonIgnoreUnknownProperties() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String json = "{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"timezone\":\"UTC\",\"extraField\":\"ignored\"}";

		Person deserialized = mapper.readValue( json, Person.class );
		assertThat( deserialized.getName() ).isEqualTo( "Alice" );
		assertThat( deserialized.getEmail() ).isEqualTo( "alice@example.com" );
		assertThat( deserialized.getTimezone() ).isEqualTo( "UTC" );
	}

}
