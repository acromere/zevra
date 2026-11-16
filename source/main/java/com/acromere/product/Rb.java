package com.acromere.product;

import com.acromere.util.JavaUtil;
import com.acromere.util.TextUtil;
import lombok.CustomLog;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resource bundle lookup for products.
 *
 * <p>This class provides static helpers for loading localized text and
 * other resources from a product's resource bundles. Products are registered
 * with {@link #init(Product)} and looked up by the calling module, so each
 * caller resolves values from its own product first.</p>
 *
 * <p>When a value is missing, the lookup falls back to the product's parent,
 * if it has one, making it possible for a product to override only the values
 * it needs while inheriting the rest.</p>
 *
 * @see Product
 */
@CustomLog
public class Rb {

	/**
	 * The default path of the resource bundles, relative to the product
	 * package.
	 */
	private static final String DEFAULT_PATH = "bundles";

	// Map of module name to products
	private static final Map<String, Product> products;

	/**
	 * Map of product to the path of its resource bundles.
	 */
	private static final Map<Product, String> productPaths;

	static {
		products = new ConcurrentHashMap<>();
		productPaths = new ConcurrentHashMap<>();
	}

	/**
	 * Register a product so that its resource bundles can be looked up by the
	 * calling module.
	 *
	 * @param product The product to register
	 */
	public static void init( Product product ) {
		String name = product.getClass().getModule().getName();
		if( name == null ) name = product.getClass().getName();
		products.put( name, product );
		productPaths.put( product, product.getClass().getPackageName().replace( ".", "/" ) );
	}

	/**
	 * Get the bundle path for the calling product using the default path.
	 *
	 * @return The bundle path for the calling product
	 */
	public static String getPath() {
		return getPath( getProduct() );
	}

	/**
	 * Get the bundle path for the specified product using the default path.
	 *
	 * @param product The product whose bundle path should be returned
	 * @return The bundle path for the product
	 */
	public static String getPath( Product product ) {
		return getPath( product, DEFAULT_PATH );
	}

	/**
	 * Get the bundle path for the specified product and path.
	 * <p>
	 * If the path is absolute (starts with a slash) it is returned as-is;
	 * otherwise it is resolved relative to the product's bundle path.
	 * </p>
	 *
	 * @param product The product whose bundle path should be used
	 * @param path The path, absolute or relative to the product's bundle path
	 * @return The resolved bundle path
	 */
	public static String getPath( Product product, String path ) {
		String bundlePath = productPaths.get( product );
		if( bundlePath == null ) log.atWarning().log( "Product RB not registered: %s", product.getClass().getName() );
		return path.startsWith( "/" ) ? path : bundlePath + "/" + path;
	}

	/**
	 * Get a localized value from the calling product's resource bundles.
	 *
	 * @param rbKey The resource bundle key
	 * @param valueKey The key of the value within the bundle
	 * @param values The values to substitute into the formatted string
	 * @return The localized value, or null if it is not found
	 */
	public static String text( String rbKey, String valueKey, Object... values ) {
		return doGetText( getProduct(), DEFAULT_PATH, rbKey, valueKey, false, null, values );
	}

	/**
	 * Get a localized value from the specified product's resource bundles.
	 *
	 * @param product The product whose resource bundles should be searched
	 * @param rbKey The resource bundle key
	 * @param valueKey The key of the value within the bundle
	 * @param values The values to substitute into the formatted string
	 * @return The localized value, or null if it is not found
	 */
	public static String text( Product product, String rbKey, String valueKey, Object... values ) {
		return doGetText( product, DEFAULT_PATH, rbKey, valueKey, false, null, values );
	}

	/**
	 * Get a localized value from the calling product's resource bundles,
	 * falling back to the specified value if the value is not found.
	 *
	 * @param rbKey The resource bundle key
	 * @param valueKey The key of the value within the bundle
	 * @param other The value to return if the value is not found
	 * @param values The values to substitute into the formatted string
	 * @return The localized value, or the specified value if it is not found
	 */
	public static String textOr( String rbKey, String valueKey, String other, Object... values ) {
		return doGetText( getProduct(), DEFAULT_PATH, rbKey, valueKey, true, other, values );
	}

	/**
	 * Get a localized value from the specified product's resource bundles,
	 * falling back to the specified value if the value is not found.
	 *
	 * @param product The product whose resource bundles should be searched
	 * @param rbKey The resource bundle key
	 * @param valueKey The key of the value within the bundle
	 * @param other The value to return if the value is not found
	 * @param values The values to substitute into the formatted string
	 * @return The localized value, or the specified value if it is not found
	 */
	public static String textOr( Product product, String rbKey, String valueKey, String other, Object... values ) {
		return doGetText( product, DEFAULT_PATH, rbKey, valueKey, true, other, values );
	}

	/**
	 * Get the list of localized resource paths for the specified resource
	 * path.
	 * <p>
	 * The list contains the resource path with each progressively shorter
	 * locale suffix, followed by the resource path with no locale suffix, in
	 * that order.
	 * </p>
	 *
	 * @param resourcePath The base resource path
	 * @return The list of localized resource paths
	 */
	public static List<String> localizedPaths( String resourcePath ) {
		return localizedPaths( resourcePath, "" );
	}

	/**
	 * Get the list of localized resource paths for the specified resource
	 * path and suffix.
	 * <p>
	 * The list contains the resource path with the suffix appended and each
	 * progressively shorter locale suffix, followed by the resource path with
	 * the suffix appended and no locale suffix, in that order.
	 * </p>
	 *
	 * @param resourcePath The base resource path
	 * @param resourceSuffix The suffix to append to each path
	 * @return The list of localized resource paths
	 */
	public static List<String> localizedPaths( String resourcePath, String resourceSuffix ) {
		List<String> names = new ArrayList<>();

		String localeSuffix = "_" + Locale.getDefault().toString();

		while( TextUtil.isNotEmpty( localeSuffix ) ) {
			// Add name
			names.add( resourcePath + localeSuffix + resourceSuffix );

			// Trim suffix
			localeSuffix = localeSuffix.substring( 0, localeSuffix.lastIndexOf( "_" ) );
		}

		// Add remaining name
		names.add( resourcePath + localeSuffix + resourceSuffix );

		return names;
	}

	private static String doGetText( Product product, String path, String rbKey, String valueKey, boolean useOther, String other, Object... values ) {
		return doGetText( product, product, path, rbKey, valueKey, useOther, other, values );
	}

	private static String doGetText(
		final Product originalProduct,
		final Product product,
		final String path,
		final String rbKey,
		final String valueKey,
		final boolean useOther,
		final String other,
		final Object... values
	) {
		if( product == null ) return other;

		String string = null;
		Product parent = product.getParent();
		String productPath = getPath( product, path );
		String rbPath = productPath + "/" + rbKey;
		String missingResourceMessage = originalProduct.getCard().getArtifact() + " > " + rbKey + " > " + valueKey;

		try {
			ResourceBundle bundle = getResourceBundle( product, rbPath );
			if( valueKey != null && bundle.containsKey( valueKey ) ) string = MessageFormat.format( bundle.getString( valueKey ), values );
		} catch( MissingResourceException exception ) {
			log.atWarning().log( "Missing bundle for: %s", rbPath );
			log.atConfig().log( "Is module package open: %s", productPath.replace( "/", "." ) );
			if( parent == null ) {
				log.atWarning().log( "Missing parent bundle for: %s", rbPath );
				return null;
			}
		}

		if( string == null ) {
			if( parent != null ) {
				string = doGetText( originalProduct, parent, path, rbKey, valueKey, useOther, other, values );
			} else {
				if( useOther ) {
					string = other;
				} else {
					String prompt = "Missing value for: %s";
					string = missingResourceMessage;
					if( log.atDebug().isEnabled() ) {
						log.atWarning().withCause( new MissingResourceException( string, rbKey, string ) ).log( prompt, string );
					} else {
						log.atWarning().log( prompt, string );
					}
				}
			}
		}

		return string;
	}

	private static Product getProduct() {
		return products.get( JavaUtil.getCallingModuleName() );
	}

	private static ResourceBundle getResourceBundle( Product product, String path ) {
		return ResourceBundle.getBundle( path, Locale.getDefault(), product.getClass().getModule() );
	}

}
