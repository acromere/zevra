package com.acromere.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContributorTest {

	@Test
	void testProperties() {
		Contributor contributor = new Contributor();
		contributor.setName( "John Doe" );
		contributor.setEmail( "john@example.com" );
		contributor.setTimezone( "UTC" );
		contributor.setOrganization( "Acromere" );
		contributor.setOrganizationUrl( "https://acromere.com" );
		contributor.setRoles( List.of( "Developer", "Tester" ) );

		assertThat( contributor.getName() ).isEqualTo( "John Doe" );
		assertThat( contributor.getEmail() ).isEqualTo( "john@example.com" );
		assertThat( contributor.getTimezone() ).isEqualTo( "UTC" );
		assertThat( contributor.getOrganization() ).isEqualTo( "Acromere" );
		assertThat( contributor.getOrganizationUrl() ).isEqualTo( "https://acromere.com" );
		assertThat( contributor.getRoles() ).containsExactly( "Developer", "Tester" );
	}

	@Test
	void testDefaultRoles() {
		Contributor contributor = new Contributor();
		assertThat( contributor.getRoles() ).isNotNull();
		assertThat( contributor.getRoles() ).isEmpty();
	}

	@Test
	void testToString() {
		Contributor contributor = new Contributor();
		contributor.setName( "Jane Doe" );
		contributor.setRoles( List.of( "Designer" ) );

		assertThat( contributor.toString() ).isEqualTo( "Contributor{name='Jane Doe roles=[Designer]}" );
	}

	@Test
	void testJsonSerialization() throws Exception {
		Contributor contributor = new Contributor();
		contributor.setName( "Alice" );
		contributor.setEmail( "alice@example.com" );
		contributor.setOrganization( "Acme Corp" );
		contributor.setOrganizationUrl( "https://acme.org" );
		contributor.setRoles( List.of( "Admin" ) );

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString( contributor );

		assertThat( json ).contains( "\"name\":\"Alice\"" );
		assertThat( json ).contains( "\"email\":\"alice@example.com\"" );
		assertThat( json ).contains( "\"organization\":\"Acme Corp\"" );
		assertThat( json ).contains( "\"organizationUrl\":\"https://acme.org\"" );
		assertThat( json ).contains( "\"roles\":[\"Admin\"]" );
		assertThat( json ).doesNotContain( "timezone" );
	}

	@Test
	void testJsonDeserialization() throws Exception {
		String json = "{\"name\":\"Bob\",\"organization\":\"Example\",\"roles\":[\"Dev\"],\"unknownField\":\"value\"}";
		ObjectMapper mapper = new ObjectMapper();
		Contributor contributor = mapper.readValue( json, Contributor.class );

		assertThat( contributor.getName() ).isEqualTo( "Bob" );
		assertThat( contributor.getOrganization() ).isEqualTo( "Example" );
		assertThat( contributor.getRoles() ).containsExactly( "Dev" );
		assertThat( contributor.getEmail() ).isNull();
	}

}
