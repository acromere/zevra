package com.acromere.product;

import com.acromere.util.UriUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class represents the product information. The product information
 * includes group, name, version, etc.
 * <p>
 * Product information is stored
 * in two files, one that loads quickly (properties file), but does not support
 * hierarchical data, and one that supports hierarchical data (JSON file), but
 * loads much more slowly.
 * </p>
 * <p>
 * This class must load the product "info" very quickly. The full product "card"
 * can load more slowly.
 * </p>
 */
@Getter
@Setter
@Accessors( chain = true )
@CustomLog
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class ProductCard extends BaseCard {

	private static final String PRODUCT_CARD = "META-INF/product.card";

	private static final String REBRAND_CARD = "lib/app/rebrand.card";

	/**
	 * The unique key of the product, formed from the group and artifact.
	 */
	@Getter
	@JsonIgnore
	@Accessors( chain = true )
	private String productKey;

	/**
	 * The group of the product.
	 */
	private String group;

	/**
	 * The artifact of the product.
	 */
	private String artifact;

	/**
	 * The version of the product.
	 */
	private String version;

	/**
	 * The timestamp of the product release.
	 */
	private String timestamp;

	/**
	 * The packaging of the product, such as the packaging type used to
	 * install the product.
	 */
	private String packaging;

	/**
	 * The version of the packaging.
	 */
	private String packagingVersion;

	/**
	 * The release of the product, formed from the version and timestamp.
	 */
	@JsonIgnore
	private Release release;

	/**
	 * The icons of the product.
	 */
	private List<String> icons;

	/**
	 * The display name of the product.
	 */
	private String name;

	/**
	 * The name of the provider of the product.
	 */
	private String provider;

	/**
	 * The url of the provider of the product.
	 */
	private String providerUrl;

	/**
	 * The year the product was first released.
	 */
	private int inception;

	/**
	 * A short description of the product.
	 */
	private String summary;

	/**
	 * A full description of the product.
	 */
	private String description;

	/**
	 * A summary of the copyright for the product.
	 */
	private String copyrightSummary;

	/**
	 * A summary of the license for the product.
	 */
	private String licenseSummary;

	/**
	 * The uri of the product pack, if the card was loaded from a remote source.
	 */
	@JsonIgnore
	private String productUri;

	/**
	 * The Java version required by the product.
	 */
	private String javaVersion;

	/**
	 * The folder the product is installed in.
	 */
	private Path installFolder;

	/**
	 * The people maintaining the product.
	 */
	private List<Maintainer> maintainers;

	/**
	 * The people contributing to the product.
	 */
	private List<Contributor> contributors;

	/**
	 * The rebrand settings for the product, if the product has been rebranded.
	 */
	private Rebrand rebrand;

	// --- For program use only ---

	/**
	 * Whether the product is enabled.
	 */
	private boolean enabled;

	/**
	 * Whether the product can be removed.
	 */
	private boolean removable;

	/**
	 * The repository the product was loaded from.
	 */
	@JsonIgnore
	private RepoCard repo;

	/**
	 * Additional resources for the product.
	 */
	@JsonIgnore
	private Map<String, String> resources;

	/**
	 * Create an empty card.
	 */
	public ProductCard() {}

	/**
	 * Load the product card from the specified folder.
	 *
	 * @param path The folder containing the product card
	 * @return The product card
	 * @throws IOException If an I/O error occurs while reading the card
	 */
	public static ProductCard card( Path path ) throws IOException {
		try( FileInputStream input = new FileInputStream( path.resolve( PRODUCT_CARD ).toFile() ) ) {
			return new ProductCard().fromJson( input );
		}
	}

	/**
	 * Load the product card of the specified product.
	 *
	 * @param product The product whose card should be loaded
	 * @return The product card of the product
	 */
	public static ProductCard card( Product product ) {
		return card( product.getClass() );
	}

	/**
	 * Load the product card associated with the specified class.
	 *
	 * @param source The class used to locate the product card resource
	 * @return The product card
	 * @throws RuntimeException If an I/O error occurs while loading the card
	 */
	public static ProductCard card( Class<?> source ) {
		try {
			return new ProductCard().fromJson( source );
		} catch( IOException exception ) {
			throw new RuntimeException( "Error loading product card: " + source.getName(), exception );
		}
	}

	/**
	 * Determines whether the application is running in a JDK-linked environment.
	 * <p>
	 * A JDK-linked environment is identified by the absence of the "jdk.module.path" system property.
	 *
	 * @return {@code true} if running in a JDK-linked environment
	 * {@code false} otherwise.
	 */
	public static boolean isJLinked() {
		return System.getProperty( "jdk.module.path" ) == null;
	}

	/**
	 * Retrieves the program's home directory by resolving the parent directory
	 * two levels above the Java installation directory (retrieved from the
	 * "java.home" system property).
	 *
	 * @return the resolved {@code Path} representing the program's home directory
	 */
	public static Path getProgramHome() throws IOException {
		return Paths.get( System.getProperty( "java.home" ) ).getParent().getParent();
	}

	/**
	 * Loads product information as a {@code ProductCard} object using either a
	 * resource stream associated with the provided class or a "rebrand" card
	 * file. If using a "rebrand" card file, the <code>rebrand.card</code> file
	 * is located in <code>${JAVA_HOME}/lib/app</code> when the program is
	 * packaged with <code>jpackage</code>.
	 *
	 * @param clazz the {@code Class<?>} object used to locate the product card resource
	 * @return a {@code ProductCard} instance populated with product details from the resource
	 * @throws IOException if an I/O error occurs while accessing or reading the resource
	 */
	private ProductCard fromJson( Class<?> clazz ) throws IOException {
		/*
		 * NOTE Using the class loader instead of the class to find the resource
		 * does not work as expected when loading products from the classpath.
		 */
		Path rebrandInfoFile = getProgramHome().resolve( REBRAND_CARD );
		if( Files.exists( rebrandInfoFile ) ) {
			try( InputStream infoInput = new FileInputStream( rebrandInfoFile.toFile() ) ) {
				return fromJson( infoInput );
			}
		} else {
			try( InputStream infoInput = clazz.getResourceAsStream( "/" + PRODUCT_CARD ) ) {
				return fromJson( infoInput );
			}
		}
	}

	private ProductCard fromJson( InputStream input ) throws IOException {
		return fromJson( input, null );
	}

	/**
	 * Read a card from a JSON stream and copy it into this card.
	 *
	 * @param input The stream containing the card JSON
	 * @param source The source of the stream, or null
	 * @return This card, populated with the values read from the stream
	 * @throws IOException If an I/O error occurs while reading the stream
	 * @throws NullPointerException If the input stream is null
	 */
	public ProductCard fromJson( InputStream input, URI source ) throws IOException {
		if( input == null ) throw new NullPointerException( "InputStream cannot be null" );
		ProductCard card = new ObjectMapper().readerFor( new TypeReference<ProductCard>() {} ).readValue( input );
		if( source != null ) this.productUri = UriUtil.removeQueryAndFragment( source ).toString();
		return copyFrom( card );
	}

	/**
	 * Copy the fields of the specified card into this card.
	 * <p>
	 * The product key and release are recomputed from the copied group,
	 * artifact, version, and timestamp.
	 * </p>
	 *
	 * @param card The card to copy from
	 * @return This card
	 */
	public ProductCard copyFrom( ProductCard card ) {
		this.group = card.group;
		this.artifact = card.artifact;
		this.version = card.version;
		this.timestamp = card.timestamp;

		this.packaging = card.packaging;
		this.packagingVersion = card.packagingVersion;

		this.icons = card.icons;
		this.name = card.name;
		this.provider = card.provider;
		this.providerUrl = card.providerUrl;
		this.inception = card.inception;

		this.summary = card.summary;
		this.description = card.description;
		this.copyrightSummary = card.copyrightSummary;
		this.licenseSummary = card.licenseSummary;

		this.javaVersion = card.javaVersion;

		if( card.maintainers != null ) this.maintainers = new ArrayList<>( card.maintainers );
		if( card.contributors != null ) this.contributors = new ArrayList<>( card.contributors );

		this.rebrand = card.rebrand;

		this.enabled = card.enabled;
		this.removable = card.removable;

		if( card.resources != null ) this.resources = new HashMap<>( card.resources );

		this.updateKey();
		this.updateRelease();

		return this;
	}

	/**
	 * Set the group of the product and update the product key.
	 *
	 * @param group The group of the product
	 * @return This card
	 */
	public ProductCard setGroup( String group ) {
		this.group = group;
		updateKey();
		return this;
	}

	/**
	 * Set the artifact of the product and update the product key.
	 *
	 * @param artifact The artifact of the product
	 * @return This card
	 */
	public ProductCard setArtifact( String artifact ) {
		this.artifact = artifact;
		updateKey();
		return this;
	}

	/**
	 * Set the version of the product and update the release.
	 *
	 * @param version The version of the product
	 * @return This card
	 */
	public ProductCard setVersion( String version ) {
		this.version = version;
		updateRelease();
		return this;
	}

	/**
	 * Set the timestamp of the product release and update the release.
	 *
	 * @param timestamp The timestamp of the product release
	 * @return This card
	 */
	public ProductCard setTimestamp( String timestamp ) {
		this.timestamp = timestamp;
		updateRelease();
		return this;
	}

	/**
	 * Set the packaging of the product.
	 *
	 * @param packaging The packaging of the product
	 * @return This card
	 */
	public ProductCard setPackaging( String packaging ) {
		this.packaging = packaging;
		return this;
	}

	/**
	 * Set the version of the packaging.
	 *
	 * @param version The version of the packaging
	 * @return This card
	 */
	public ProductCard setPackagingVersion( String version ) {
		this.packagingVersion = version;
		return this;
	}

	/**
	 * Set the icons of the product. The icons list is unmodifiable.
	 *
	 * @param icons The icons of the product, or null for no icons
	 * @return This card
	 */
	public ProductCard setIcons( List<String> icons ) {
		this.icons = Collections.unmodifiableList( icons == null ? List.of() : icons );
		return this;
	}

	/**
	 * Get the uris of the resources needed by the product on the current
	 * platform.
	 *
	 * @return The uris of the product resources
	 */
	@JsonIgnore
	public String[] getResourceUris() {
		return getPlatformResourceUris();
	}

	private void updateKey() {
		/*
		 * The use of '.' as the separator is the most benign of the characters
		 * tested. Changing the separator to a different character will most likely
		 * result in invalid file paths, setting paths, and other undesired side
		 * effects.
		 */
		if( group != null && artifact != null ) productKey = group + "." + artifact;
	}

	private void updateRelease() {
		release = Release.create( this.version, this.timestamp );
	}

	private String[] getPlatformResourceUris() {
		String os = System.getProperty( "os.name" );
		String arch = System.getProperty( "os.arch" );

		Set<String> resources = new HashSet<>();

		// Add the product pack URI
		resources.add( getProductUri() );

		// This code was originally intended to resolve os/architecture specific
		// resources needed for a product. For the moment, this feature is not
		// needed and this method simply returns an empty set.
		//
		//		path += "/@uri";
		//
		//		// Determine the resources.
		//		Node[] nodes = descriptor.getNodes( ProductCard.RESOURCES_PATH );
		//		for( Node node : nodes ) {
		//			XmlDescriptor resourcesDescriptor = new XmlDescriptor( node );
		//			Node osNameNode = node.getAttributes().getNamedItem( "os" );
		//			Node osArchNode = node.getAttributes().getNamedItem( "arch" );
		//
		//			String osName = osNameNode == null ? null : osNameNode.getTextContent();
		//			String osArch = osArchNode == null ? null : osArchNode.getTextContent();
		//
		//			// Determine what resources should not be included.
		//			if( osName != null && !os.startsWith( osName ) ) continue;
		//			if( osArch != null && !arch.equals( osArch ) ) continue;
		//
		//			uris = resourcesDescriptor.getValues( path );
		//			if( uris != null ) resources.addAll( Arrays.asList( uris ) );
		//		}

		return resources.toArray( new String[ 0 ] );
	}

	/**
	 * Return the product key.
	 *
	 * @return The product key of this card
	 */
	@Override
	public String toString() {
		return getProductKey();
	}

	/**
	 * The hash code is derived from the product key.
	 *
	 * @return The hash code of the product key
	 */
	@Override
	public int hashCode() {
		return getProductKey().hashCode();
	}

	/**
	 * Two cards are equal if their product keys are equal.
	 *
	 * @param object The object to compare with
	 * @return {@code true} if the specified object is a card with the same product key
	 */
	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof ProductCard that) ) return false;
		return this.getProductKey().equals( that.getProductKey() );
	}

	/**
	 * Deeply compare this card to the specified object.
	 * <p>
	 * Unlike {@link #equals(Object)}, this method compares the full content
	 * of the card, not just the product key.
	 * </p>
	 *
	 * @param object The object to compare with
	 * @return {@code true} if the specified object is a card with the same content
	 */
	public boolean deepEquals( Object object ) {
		if( !(object instanceof ProductCard that) ) return false;

		boolean equals = this.group.equals( that.group );
		equals = equals && this.artifact.equals( that.artifact );
		equals = equals && this.packaging.equals( that.packaging );
		equals = equals && this.release.equals( that.release );
		equals = equals && this.icons.equals( that.icons );
		equals = equals && this.name.equals( that.name );
		equals = equals && this.provider.equals( that.provider );
		equals = equals && this.inception == that.inception;
		equals = equals && this.summary.equals( that.summary );
		equals = equals && this.description.equals( that.description );
		equals = equals && this.copyrightSummary.equals( that.copyrightSummary );
		equals = equals && this.licenseSummary.equals( that.licenseSummary );
		equals = equals && this.productUri.equals( that.productUri );

		return equals;
	}

}
