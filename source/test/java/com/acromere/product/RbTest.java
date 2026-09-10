package com.acromere.product;

import com.acromere.producta.MockProductA;
import com.acromere.productb.MockProductB;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class RbTest {

	@Test
	void testGetPathRelative() {
		Product product = new MockProduct();
		assertThat( Rb.getPath( product, "action" ) ).isEqualTo( getClass().getPackageName().replace( ".", "/" ) + "/action" );
	}

	@Test
	void testGetPathAbsolute() {
		Product product = new MockProduct();
		assertThat( Rb.getPath( product, "/action" ) ).isEqualTo( "/action" );
	}

	@Test
	void testText() {
		Product product = new MockProductA();
		assertThat( Rb.text( product, "test", null ) ).isEqualTo( "mock-product-a > test > null" );
		assertThat( Rb.text( product, "test", "invalid" ) ).isEqualTo( "mock-product-a > test > invalid" );
		assertThat( Rb.text( product, "test", "name" ) ).isEqualTo( "Product A" );
	}

	@Test
	void testTextOr() {
		Product product = new MockProductB( new MockProductA() );
		assertThat( Rb.textOr( product, "test", null, "" ) ).isEqualTo( "" );
		assertThat( Rb.textOr( product, "test", "invalid", "" ) ).isEqualTo( "" );
		assertThat( Rb.textOr( product, "test", "name", "" ) ).isEqualTo( "Product B" );
	}

	@Test
	void testTextOrWithParentProduct() {
		Product productA = new MockProductA();
		MockProductB productB = new MockProductB( new MockProduct() );

		// Make sure bundleB does not have a theme-color, it should use the one from productA
		assertThat( productB.getTheme() ).isNull();

		// Now check that the productA theme-color can be retrieved using productB
		productB = new MockProductB( productA );
		assertThat( productB.getTheme() ).isEqualTo( "blue" );
	}

	@Test
	void testLocalizedPaths() {
		Locale original = Locale.getDefault();
		try {
			Locale.setDefault( Locale.of( "en", "US" ) );
			assertThat( Rb.localizedPaths( "path/to/resource", ".txt" ) ).containsExactly(
				"path/to/resource_en_US.txt",
				"path/to/resource_en.txt",
				"path/to/resource.txt"
			);

			Locale.setDefault( Locale.of( "en" ) );
			assertThat( Rb.localizedPaths( "path/to/resource", ".txt" ) ).containsExactly(
				"path/to/resource_en.txt",
				"path/to/resource.txt"
			);

			Locale.setDefault( Locale.of( "en", "US", "variant" ) );
			assertThat( Rb.localizedPaths( "path/to/resource", ".txt" ) ).containsExactly(
				"path/to/resource_en_US_variant.txt",
				"path/to/resource_en_US.txt",
				"path/to/resource_en.txt",
				"path/to/resource.txt"
			);
		} finally {
			Locale.setDefault( original );
		}
	}

}
