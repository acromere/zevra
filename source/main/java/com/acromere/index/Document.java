package com.acromere.index;

import com.acromere.util.TokenReplacingReader;
import lombok.CustomLog;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The content to be indexed, along with metadata about the content.
 *
 * <p>The content can either be set inline as a {@link #content string} or
 * loaded from a {@link #url URL}. The {@link #mediaType media type}
 * indicates how the content should be parsed, for example so HTML tags are
 * not indexed. Replacement {@link #values values} are substituted into the
 * content before the content is indexed.</p>
 */
@Data
@Accessors( fluent = true )
@CustomLog
public class Document {

	/**
	 * The media type of the document. This is set to SupportedMediaType.TEXT by
	 * default but should be changed to match the media type of the document. For
	 * example, when indexing an HTML document, the media type should be set to
	 * SupportedMediaType.HTML so the HTML tags are not indexed.
	 */
	private MediaType mediaType;

	/**
	 * The document URI. The URI is a unique identifier for this document,
	 * regardless of language, replacement values, or the location of the content.
	 * The uri is different from the {@link #url}, but is often related.
	 */
	private URI uri;

	/**
	 * The icon key or URL.
	 */
	private String icon;

	/**
	 * The document title.
	 */
	private String title;

	/**
	 * The document tags.
	 */
	private Set<String> tags;

	/**
	 * The URL from where to load the document content. The url is different from
	 * the {@link #uri}, but is often related.
	 */
	private URL url;

	/**
	 * The content loaded from the {@link #url}. This is the data that will be
	 * indexed.
	 */
	private String content;

	/**
	 * The replacement values for the content. These are to be replaced in the
	 * content before the content is indexed.
	 */
	private Map<String, String> values;

	/**
	 * The document properties. These properties have no special meaning to the
	 * indexer but are available to store metadata regarding the document.
	 */
	private Map<String, Object> properties;

	/**
	 * The search points for this document. The points indicate how well this
	 * document matched the search terms.
	 */
	@EqualsAndHashCode.Exclude
	private int searchPoints;

	/**
	 * The search priority for this document. The priority indicates the
	 * importance of this document's search hits.
	 */
	@EqualsAndHashCode.Exclude
	private int searchPriority;

	/**
	 * Create a document with no URI, icon, or title.
	 */
	public Document() {
		this( null, null, null );
	}

	/**
	 * Create a document with a URI, icon, and title. The media type defaults
	 * to {@link MediaType#TEXT}.
	 *
	 * @param uri   The document URI
	 * @param icon  The icon key or URL
	 * @param title The document title
	 */
	public Document( URI uri, String icon, String title ) {
		this.uri = uri;
		this.icon = icon;
		this.title = title;
		this.mediaType = MediaType.TEXT;
	}

	/**
	 * Create a document with a URI, icon, title, and the URL from where to
	 * load the content. The media type defaults to {@link MediaType#TEXT}.
	 *
	 * @param uri   The document URI
	 * @param icon  The icon key or URL
	 * @param title The document title
	 * @param url   The URL from where to load the document content
	 */
	public Document( URI uri, String icon, String title, URL url ) {
		this( uri, icon, title );
		url( url );
	}

	/**
	 * Create a document with a URI, icon, title, and inline content. The
	 * media type defaults to {@link MediaType#TEXT}.
	 *
	 * @param uri     The document URI
	 * @param icon    The icon key or URL
	 * @param title   The document title
	 * @param content The document content
	 */
	public Document( URI uri, String icon, String title, String content ) {
		this( uri, icon, title );
		content( content );
	}

	/**
	 * Get a copy of the document tags.
	 *
	 * @return A set of the document tags
	 */
	public Set<String> tags() {
		return new HashSet<>( tags == null ? Set.of() : tags );
	}

	/**
	 * Add tags to the document.
	 *
	 * @param tags The tags to add
	 * @return This document
	 */
	public Document tags( Collection<String> tags ) {
		if( this.tags == null ) this.tags = new CopyOnWriteArraySet<>();
		this.tags.addAll( tags );
		return this;
	}

	/**
	 * Get the document properties. The properties map is created on first
	 * access.
	 *
	 * @return A map of the document properties
	 */
	public Map<String, Object> properties() {
		if( properties == null ) properties = new ConcurrentHashMap<>();
		return properties;
	}

	/**
	 * Get a reader for the document content. If the content is loaded from a
	 * URL, the reader reads from the URL, otherwise the reader reads the
	 * inline content. The {@link #values replacement values} are applied to
	 * the content by the reader.
	 *
	 * @return A reader for the document content, or null if the document has
	 *         no content
	 * @throws IOException If the content cannot be read
	 */
	public Reader reader() throws IOException {
		Reader reader;

		if( url != null ) {
			reader = new InputStreamReader( url.openStream(), StandardCharsets.UTF_8 );
		} else if( content != null ) {
			reader = new StringReader( content );
		} else {
			return null;
		}

		return new TokenReplacingReader( reader, values );
	}

	/**
	 * The media types supported by the index.
	 */
	public enum MediaType {

		/**
		 * Plain text. The content is indexed as is.
		 */
		TEXT,

		/**
		 * HTML. The HTML tags are not indexed.
		 */
		HTML
	}

}
