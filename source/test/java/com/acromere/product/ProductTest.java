package com.acromere.product;

import com.acromere.settings.Settings;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

	private static class CustomProduct implements Product {

		private final Product parent;

		public CustomProduct( Product parent ) {
			this.parent = parent;
		}

		@Override
		public ProductCard getCard() {
			return new ProductCard().setName( "Custom" );
		}

		@Override
		public Settings getSettings() {
			return null;
		}

		@Override
		public Path getDataFolder() {
			return Path.of( "/tmp/product" );
		}

		@Override
		public Product getParent() {
			return parent;
		}

	}

	@Test
	void testDefaultGetParent() {
		Product product = new Product() {
			@Override
			public ProductCard getCard() {
				return null;
			}

			@Override
			public Settings getSettings() {
				return null;
			}

			@Override
			public Path getDataFolder() {
				return null;
			}
		};

		assertThat( product.getParent() ).isNull();
	}

	@Test
	void testCustomProductWithParent() {
		Product parent = new CustomProduct( null );
		Product child = new CustomProduct( parent );

		assertThat( child.getParent() ).isSameAs( parent );
		assertThat( child.getCard().getName() ).isEqualTo( "Custom" );
		assertThat( child.getDataFolder() ).isEqualTo( Path.of( "/tmp/product" ) );
	}

}
