package com.acromere.productb;

import com.acromere.product.Product;
import com.acromere.product.ProductCard;
import com.acromere.product.Rb;
import com.acromere.settings.Settings;

import java.nio.file.Path;

public class MockProductB implements Product {

	private final Product parent;

	private ProductCard card;

	public MockProductB( Product parent ) {
		this.parent = parent;
		Rb.init( this );
	}

	@Override
	public ProductCard getCard() {
		if( card == null ) card = new ProductCard().setArtifact( "mock-product-b" ).setName( "Mock Product B" );
		return card;
	}

	@Override
	public Product getParent() {
		return parent;
	}

	@Override
	public Settings getSettings() {
		return null;
	}

	@Override
	public Path getDataFolder() {
		return null;
	}

	public String getTheme() {
		return Rb.textOr( this, "test", "theme-color", null );
	}

}
