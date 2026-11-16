package com.acromere.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * A descriptor for a repository catalog.
 *
 * <p>A catalog card extends {@link RepoCard} with a timestamp and the set of
 * product artifact ids available in the repository. Catalogs are stored in a
 * {@value #FILE} file at the root of a repository.</p>
 *
 * @see RepoCard
 */
@Getter
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class CatalogCard extends RepoCard {

	/**
	 * The name of the catalog file in a repository.
	 */
	public static final String FILE = "catalog.card";

	/**
	 * The time the catalog was created, in milliseconds since the epoch.
	 */
	@Setter
	private long timestamp;

	/**
	 * The set of products in the repository.
	 * <p>
	 * The set contains the product artifact ids only.
	 */
	private Set<String> products = new HashSet<>();

	/**
	 * Create an empty catalog card.
	 */
	public CatalogCard() {}

	/**
	 * Create a catalog card from a JSON stream.
	 *
	 * @param input The stream containing the card JSON
	 * @return The card read from the stream
	 * @throws IOException If an I/O error occurs while reading the stream
	 */
	public static CatalogCard fromJson( InputStream input ) throws IOException {
		return new ObjectMapper().readerFor( new TypeReference<CatalogCard>() {} ).readValue( input );
	}

	/**
	 * Set the set of products in the repository.
	 *
	 * @param products The product artifact ids, or null to clear the set
	 */
	public void setProducts( Set<String> products ) {
		this.products = products == null ? Set.of() : new HashSet<>( products );
	}

	/**
	 * Copy the fields of the specified card into this card.
	 *
	 * @param card The card to copy from
	 * @return This card, or null if the specified card is null
	 */
	@Override
	public RepoCard copyFrom( RepoCard card ) {
		super.copyFrom( card );
		if( card == null ) return null;
		((CatalogCard)card).setTimestamp( timestamp );
		((CatalogCard)card).setProducts( products );
		return this;
	}

}
