package com.acromere.producta;

import com.acromere.product.Product;
import com.acromere.product.ProductCard;
import com.acromere.product.Rb;
import com.acromere.settings.Settings;

import java.nio.file.Path;

public class MockProductA implements Product {

	private ProductCard card;

	private Product parent;

	public MockProductA() {
		Rb.init(this);
	}

	@Override
	public ProductCard getCard() {
		if( card == null ) card = new ProductCard().setArtifact( "mock-product-a" ).setName( "Mock Product A" );
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
}
