package com.acromere.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RebrandTest {

	@Test
	void testProperties() {
		Rebrand rebrand = new Rebrand();
		rebrand.setSplashScreenBackgroundClass( "com.acromere.splash.Bg" );
		rebrand.setSplashScreenTitleFontSize( 24.5 );
		rebrand.setProductIconClass( "com.acromere.icon.Icon" );
		rebrand.setModuleClasses( List.of( "com.acromere.module.A", "com.acromere.module.B" ) );

		assertThat( rebrand.getSplashScreenBackgroundClass() ).isEqualTo( "com.acromere.splash.Bg" );
		assertThat( rebrand.getSplashScreenTitleFontSize() ).isEqualTo( 24.5 );
		assertThat( rebrand.getProductIconClass() ).isEqualTo( "com.acromere.icon.Icon" );
		assertThat( rebrand.getModuleClasses() ).containsExactly( "com.acromere.module.A", "com.acromere.module.B" );
	}

	@Test
	void testJsonSerialization() throws Exception {
		Rebrand rebrand = new Rebrand();
		rebrand.setSplashScreenBackgroundClass( "com.acromere.splash.Bg" );
		rebrand.setSplashScreenTitleFontSize( 18.0 );
		rebrand.setProductIconClass( "com.acromere.icon.Icon" );
		rebrand.setModuleClasses( List.of( "com.acromere.module.A" ) );

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString( rebrand );

		assertThat( json ).contains( "\"splashScreenBackgroundClass\":\"com.acromere.splash.Bg\"" );
		assertThat( json ).contains( "\"splashScreenTitleFontSize\":18.0" );
		assertThat( json ).contains( "\"productIconClass\":\"com.acromere.icon.Icon\"" );
		assertThat( json ).contains( "\"moduleClasses\":[\"com.acromere.module.A\"]" );
	}

	@Test
	void testJsonDeserialization() throws Exception {
		String json = "{\"splashScreenBackgroundClass\":\"BgClass\",\"splashScreenTitleFontSize\":20.0,\"extraProperty\":\"ignoreMe\"}";
		ObjectMapper mapper = new ObjectMapper();
		Rebrand rebrand = mapper.readValue( json, Rebrand.class );

		assertThat( rebrand.getSplashScreenBackgroundClass() ).isEqualTo( "BgClass" );
		assertThat( rebrand.getSplashScreenTitleFontSize() ).isEqualTo( 20.0 );
		assertThat( rebrand.getProductIconClass() ).isNull();
		assertThat( rebrand.getModuleClasses() ).isNull();
	}

}
