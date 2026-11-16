package com.acromere.product;

import com.acromere.util.OperatingSystem;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * A class loader for a product's classes, resources, and native libraries.
 *
 * <p>This loader loads classes from the product's urls first and then
 * delegates to a parent class loader, and does the same for resources. It
 * also locates native libraries packaged with the product so that a module
 * can ship the native code it needs.</p>
 *
 * @see URLClassLoader
 */
public class ProductClassLoader extends URLClassLoader {

	/**
	 * The class loader to delegate to when a class or resource is not found
	 * in the product's urls.
	 */
	private final ClassLoader parent;

	/**
	 * The codebase of the product, used to locate native libraries.
	 */
	private final URI codebase;

	/**
	 * Create a class loader for the product.
	 *
	 * @param urls The urls of the product's classes and resources
	 * @param parent The class loader to delegate to, or null
	 * @param codebase The codebase of the product, used to locate native
	 *     libraries
	 */
	public ProductClassLoader( URL[] urls, ClassLoader parent, URI codebase ) {
		super( urls, null );
		this.parent = parent;
		this.codebase = codebase;
	}

	/**
	 * Change the default class loader behavior to load module classes from the
	 * module class loader first then delegate to the parent class loader if the
	 * class could not be found.
	 */
	@Override
	public Class<?> loadClass( final String name ) throws ClassNotFoundException {
		Class<?> type = null;
		ClassNotFoundException exception = null;

		try {
			type = super.loadClass( name );
		} catch( ClassNotFoundException cnf ) {
			exception = cnf;
		}

		if( type == null ) {
			try {
				type = parent.loadClass( name );
			} catch( ClassNotFoundException cnf ) {
				exception = cnf;
			}
		}

		if( type == null ) {
			throw exception == null ? new ClassNotFoundException( name ) : exception;
		} else {
			resolveClass( type );
		}

		return type;
	}

	/**
	 * Get the url of the resource with the specified name, searching the
	 * product's urls first and then the parent class loader.
	 *
	 * @param name The name of the resource
	 * @return The url of the resource, or null if it is not found
	 */
	public URL getResource( String name ) {
		URL url = super.findResource( name );
		if( url == null ) url = parent.getResource( name );
		return url;
	}

	/**
	 * Used to find native library files used with modules. This allows a module
	 * to package needed native libraries in the module and be loaded at runtime.
	 */
	@Override
	protected String findLibrary( String name ) {
		File file = new File( codebase.resolve( "lib/" + OperatingSystem.resolveNativeLibPath( name ) ) );
		return file.exists() ? file.toString() : super.findLibrary( name );
	}

}
