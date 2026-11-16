package com.acromere.product;

import com.acromere.util.UriUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * A descriptor for a product repository.
 *
 * <p>A repository card identifies a source of product artifacts by name and
 * url. Cards are compared and hashed by their url only, so two cards with the
 * same url are considered the same repository even if their names differ.</p>
 *
 * @see CatalogCard
 * @see ProductCard
 */
@Getter
@Setter
@Accessors( chain = true )
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class RepoCard extends BaseCard {

	/**
	 * The name of the repository.
	 */
	private String name;

	/**
	 * The base url of the repository.
	 */
	private String url;

	/**
	 * The icons of the repository.
	 */
	private List<String> icons;

	/**
	 * Create a card with a null url.
	 */
	public RepoCard() {
		this( null );
	}

	/**
	 * Create a card with the specified url.
	 *
	 * @param url The base url of the repository
	 */
	public RepoCard( String url ) {
		this.url = url;
		this.icons = List.of();
	}

	/**
	 * Create a card from a JSON stream.
	 *
	 * @param input The stream containing the card JSON
	 * @param source The source of the stream, or null
	 * @return The card read from the stream
	 * @throws IOException If an I/O error occurs while reading the stream
	 */
	public static RepoCard fromJson( InputStream input, URI source ) throws IOException {
		RepoCard card = new ObjectMapper().readerFor( new TypeReference<RepoCard>() {} ).readValue( input );
		if( source != null ) card.url = UriUtil.removeQueryAndFragment( source ).toString();
		return card;
	}

	/**
	 * Copy the non-null fields of the specified card into this card.
	 *
	 * @param card The card to copy from
	 * @return This card, or null if the specified card is null
	 */
	public RepoCard copyFrom( RepoCard card ) {
		if( card == null ) return null;
		if( card.name != null ) setName( card.name );
		if( card.url != null ) setUrl( card.url );
		if( card.getIcons() != null && !card.getIcons().isEmpty() ) this.setIcons( card.getIcons() );
		return this;
	}

	/**
	 * Return the card name and url.
	 *
	 * @return A string of the form "[name] url"
	 */
	@Override
	public String toString() {
		return "[" + name + "] " + url;
	}

	/**
	 * Two cards are equal if their urls are equal.
	 *
	 * @param object The object to compare with
	 * @return {@code true} if the specified object is a card with the same url
	 */
	@Override
	public boolean equals( Object object ) {
		if( this == object ) return true;
		if( !(object instanceof RepoCard that) ) return false;
		return Objects.equals( this.url, that.url );
	}

	/**
	 * The hash code is derived from the card url.
	 *
	 * @return The hash code of the card url
	 */
	@Override
	public int hashCode() {
		return Objects.hash( url );
	}

}
