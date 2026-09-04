package com.acromere.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaintainerTest {

	@Test
	void testProperties() {
		Maintainer maintainer = new Maintainer();
		maintainer.setName( "Jane Maintainer" );
		maintainer.setEmail( "jane@example.com" );
		maintainer.setTimezone( "America/New_York" );
		maintainer.setOrganization( "Acromere Corp" );
		maintainer.setOrganizationUrl( "https://acromere.org" );
		maintainer.setRoles( List.of( "Lead Maintainer", "Architect" ) );

		assertThat( maintainer.getName() ).isEqualTo( "Jane Maintainer" );
		assertThat( maintainer.getEmail() ).isEqualTo( "jane@example.com" );
		assertThat( maintainer.getTimezone() ).isEqualTo( "America/New_York" );
		assertThat( maintainer.getOrganization() ).isEqualTo( "Acromere Corp" );
		assertThat( maintainer.getOrganizationUrl() ).isEqualTo( "https://acromere.org" );
		assertThat( maintainer.getRoles() ).containsExactly( "Lead Maintainer", "Architect" );
	}

	@Test
	void testJsonSerialization() throws Exception {
		Maintainer maintainer = new Maintainer();
		maintainer.setName( "Lead" );
		maintainer.setEmail( "lead@example.com" );
		maintainer.setRoles( List.of( "Admin" ) );

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString( maintainer );

		assertThat( json ).contains( "\"name\":\"Lead\"" );
		assertThat( json ).contains( "\"email\":\"lead@example.com\"" );
		assertThat( json ).contains( "\"roles\":[\"Admin\"]" );
		assertThat( json ).doesNotContain( "organization" );
	}

	@Test
	void testJsonDeserialization() throws Exception {
		String json = "{\"name\":\"Lead\",\"roles\":[\"Admin\"],\"extraField\":\"extra\"}";
		ObjectMapper mapper = new ObjectMapper();
		Maintainer maintainer = mapper.readValue( json, Maintainer.class );

		assertThat( maintainer.getName() ).isEqualTo( "Lead" );
		assertThat( maintainer.getRoles() ).containsExactly( "Admin" );
	}

}
