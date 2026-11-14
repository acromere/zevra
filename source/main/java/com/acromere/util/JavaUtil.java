package com.acromere.util;

import lombok.CustomLog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Utility class for Java runtime introspection, class naming, and classpath
 * handling.
 *
 * <p>Provides methods to inspect the running JVM version and classpath,
 * derive simple and shortened class names, examine the current call stack,
 * and unwrap the root cause of exceptions.</p>
 */
@CustomLog
public final class JavaUtil {

	/**
	 * Returns the major version of the running Java virtual machine.
	 *
	 * <p>The value is derived from the {@code java.version} system property.
	 * For legacy version strings starting with {@code 1.} (e.g.
	 * {@code 1.8.0_392}), the second component is returned; otherwise the
	 * first component is returned (e.g. {@code 17} for {@code 17.0.2}).</p>
	 *
	 * @return The major Java version as an integer
	 */
	public static int getVersion() {
		String version = System.getProperty( "java.version" );
		String[] parts = version.split( "\\." );
		int majorVersion = Integer.parseInt( parts[ 0 ] );
		if( majorVersion == 1 ) majorVersion = Integer.parseInt( parts[ 1 ] );
		return majorVersion;
	}

	/**
	 * Checks whether the application is running in a test environment by
	 * probing the classpath for the JUnit {@code org.junit.Assert} class.
	 *
	 * @return {@code true} if JUnit is available on the classpath,
	 * {@code false} otherwise
	 */
	public static boolean isTest() {
		try {
			JavaUtil.class.getClassLoader().loadClass( "org.junit.Assert" );
			return true;
		} catch( Throwable throwable ) {
			return false;
		}
	}

	/**
	 * Checks whether the given class appears anywhere in the current thread's
	 * stack trace.
	 *
	 * @param clazz The class to look for in the stack trace
	 * @return {@code true} if a frame of the class is present,
	 * {@code false} otherwise
	 */
	public static boolean isClassInStackTrace( Class<?> clazz ) {
		String className = clazz.getName();
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		for( StackTraceElement element : stack ) {
			if( Objects.equals( element.getClassName(), className ) ) return true;
		}

		return false;
	}

	/**
	 * Returns the fully qualified class name of the class that invoked the
	 * direct caller of this method.
	 *
	 * <p>Frames of {@code Thread}, this class, and the direct calling class
	 * are skipped, so when called from a utility or wrapper class the result
	 * identifies the code that initiated the call through that wrapper.</p>
	 *
	 * @return The fully qualified class name of the invoking class
	 */
	public static String getCallingClassName() {
		return getCallerElement().getClassName();
	}

	/**
	 * Returns the module name of the class that invoked the direct caller of
	 * this method.
	 *
	 * @return The module name of the invoking class, or {@code null} if
	 * unknown
	 */
	public static String getCallingModuleName() {
		return getCallerElement().getModuleName();
	}

	/**
	 * Returns the method name of the class that invoked the direct caller of
	 * this method.
	 *
	 * @return The method name of the invoking class
	 */
	public static String getCallingMethodName() {
		return getCallerElement().getMethodName();
	}

	/**
	 * Returns the stack frame of the class that invoked the direct caller of
	 * this method.
	 *
	 * @return The invoking class's stack trace element
	 */
	private static StackTraceElement getCallerElement() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		return elements[ getCallerIndex( elements ) ];
	}

	/**
	 * Determines the index of the invoking class's stack frame, skipping
	 * frames belonging to {@code Thread}, this class, and the direct calling
	 * class.
	 *
	 * @param elements The current thread's stack trace elements
	 * @return The index of the invoking class's stack frame
	 */
	private static int getCallerIndex( StackTraceElement[] elements ) {
		int index = 0;

		// Skip the initial elements of Thread class
		String skipClass = elements[ index ].getClassName();
		while( Objects.equals( elements[ index ].getClassName(), skipClass ) ) {
			index++;
		}

		// Skip the next elements of JavaUtil class
		skipClass = elements[ index ].getClassName();
		while( Objects.equals( elements[ index ].getClassName(), skipClass ) ) {
			index++;
		}

		// Skip the next elements of the asking class
		skipClass = elements[ index ].getClassName();
		while( Objects.equals( elements[ index ].getClassName(), skipClass ) ) {
			index++;
		}

		// Return the element with the class calling the asking class
		return index;
	}

	/**
	 * Get the simple class name from a full class name.
	 *
	 * @param name The fully qualified class name
	 * @return The class name without the package
	 */
	public static String getClassName( String name ) {
		return name == null ? null : name.substring( name.lastIndexOf( '.' ) + 1 );
	}

	/**
	 * Get the simple class name from a full class name.
	 *
	 * @param type The class from which to derive the name
	 * @return The class name without the package
	 */
	public static String getClassName( Class<?> type ) {
		return type == null ? null : getClassName( type.getName() );
	}

	/**
	 * Get only the class name from an object.
	 *
	 * @param object The object from which to derive the class name
	 * @return The class name without the package
	 */
	public static String getClassName( Object object ) {
		return object == null ? null : getClassName( object.getClass().getName() );
	}

	/**
	 * Returns a shortened form of a fully qualified class name in which each
	 * package segment is reduced to its first letter, e.g.
	 * {@code com.acromere.xenon.Program} becomes {@code c.a.x.Program}.
	 *
	 * @param name The fully qualified class name
	 * @return The shortened class name
	 */
	public static String getShortClassName( String name ) {
		StringBuilder builder = new StringBuilder();
		int index = 0;
		boolean done = false;
		do {
			String letter = name.substring( index, index + 1 );
			index = name.indexOf( ".", index + 1 );
			if( index != -1 ) {
				builder.append( letter );
				builder.append( "." );
				index++;
			} else {
				done = true;
			}
		} while( !done );
		builder.append( getClassName( name ) );

		return builder.toString();
	}

	/**
	 * Returns a shortened form of the fully qualified name of the given
	 * class.
	 *
	 * @param type The class from which to derive the name
	 * @return The shortened class name
	 * @see #getShortClassName( String )
	 */
	public static String getShortClassName( Class<?> type ) {
		return getShortClassName( type.getName() );
	}

	/**
	 * Returns a form of the class name in which nested class delimiters
	 * ({@code $}) are replaced with dots, making the name safe to use as a
	 * key or in configuration, e.g. {@code com.example.Foo$Bar} becomes
	 * {@code com.example.Foo.Bar}.
	 *
	 * @param name The fully qualified class name
	 * @return The key-safe class name
	 */
	public static String getKeySafeClassName( String name ) {
		return name.replace( "$", "." );
	}

	/**
	 * Returns a key-safe form of the name of the given class.
	 *
	 * @param type The class from which to derive the name
	 * @return The key-safe class name
	 * @see #getKeySafeClassName( String )
	 */
	public static String getKeySafeClassName( Class<?> type ) {
		return getKeySafeClassName( type.getName() );
	}

	/**
	 * Returns the package name of a fully qualified class name.
	 *
	 * @param name The fully qualified class name, which must contain a
	 *             package
	 * @return The package name
	 */
	public static String getPackageName( String name ) {
		return name.substring( 0, name.lastIndexOf( '.' ) );
	}

	/**
	 * Returns the package name of the given class.
	 *
	 * @param type The class from which to derive the package name
	 * @return The package name
	 */
	public static String getPackageName( Class<?> type ) {
		return (getPackageName( type.getName() ));
	}

	/**
	 * Returns the path form of the package of a fully qualified class name,
	 * e.g. {@code java.lang.Object} maps to {@code /java/lang}.
	 *
	 * @param name The fully qualified class name, which must contain a
	 *             package
	 * @return The package path
	 */
	public static String getPackagePath( String name ) {
		return "/" + getPackageName( name ).replace( '.', '/' );
	}

	/**
	 * Returns the path form of the package of the given class.
	 *
	 * @param type The class from which to derive the package path
	 * @return The package path
	 */
	public static String getPackagePath( Class<?> type ) {
		return (getPackagePath( type.getName() ));
	}

	/**
	 * Returns the entries of the {@code class.path} system property as a
	 * list of URIs.
	 *
	 * @return The classpath entries as URIs, or an empty list if the
	 *         property is not set
	 */
	public static List<URI> getClasspath() {
		return parsePropertyPaths( System.getProperty( "class.path" ) );
	}

	/**
	 * Returns the entries of the {@code jdk.module.path} system property as
	 * a list of URIs.
	 *
	 * @return The module path entries as URIs, or an empty list if the
	 *         property is not set
	 */
	public static List<URI> getModulePath() {
		return parsePropertyPaths( System.getProperty( "jdk.module.path" ) );
	}

	/**
	 * Parses the entries of a classpath formatted as a system property into
	 * a list of URIs, using the file path separator to delimit the entries.
	 *
	 * @param classpath The classpath string in system property format
	 * @return The classpath entries as URIs, or an empty list if the
	 *         string is {@code null}
	 * @see #parsePropertyPaths( String, String )
	 */
	public static List<URI> parsePropertyPaths( String classpath ) {
		return parsePropertyPaths( classpath, File.pathSeparator );
	}

	/**
	 * Parses the entries of a classpath in system property format into a
	 * list of URIs.
	 *
	 * <p>Each entry is URL-decoded and interpreted as a URI. If the entry
	 * is not valid URI syntax, or the resulting URI has no scheme, the
	 * entry is interpreted as a {@link File} instead.</p>
	 *
	 * @param paths The classpath string in system property format
	 * @param separator The character or string that delimits the entries
	 * @return The classpath entries as URIs, or an empty list if the
	 *         string is {@code null}
	 */
	public static List<URI> parsePropertyPaths( String paths, String separator ) {
		ArrayList<URI> list = new ArrayList<>();
		if( paths == null ) return list;

		URI uri;
		String token;
		StringTokenizer tokenizer = new StringTokenizer( paths, separator );
		while( tokenizer.hasMoreTokens() ) {
			token = tokenizer.nextToken();

			try {
				uri = new URI( UriUtil.decode( token ) );
			} catch( URISyntaxException exception ) {
				uri = new File( token ).toURI();
			}
			if( uri.getScheme() == null ) uri = new File( token ).toURI();

			list.add( uri );
		}

		return list;
	}

	/**
	 * Parses the relative URLs from a classpath in JAR file manifest format.
	 *
	 * <p>The entries are space separated and resolved against the base URI.
	 * See <a href= "http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html#Main%20Attributes"
	 * >Setting the JAR Manifest Class-Path Attribute</a>.</p>
	 *
	 * @param base The base URI against which the relative paths are
	 *             resolved
	 * @param classpath The classpath string in JAR manifest format
	 * @return The resolved URLs, or an empty list if the base URI or
	 *         classpath is {@code null}
	 * @throws IOException If a resolved path cannot be converted to a URL
	 */
	public static List<URL> parseManifestClasspath( URI base, String classpath ) throws IOException {
		List<URL> urls = new ArrayList<>();

		if( base == null || classpath == null ) return urls;

		StringTokenizer tokenizer = new StringTokenizer( classpath, " " );
		while( tokenizer.hasMoreTokens() ) {
			String path = tokenizer.nextToken();
			urls.add( base.resolve( path ).toURL() );
		}

		return urls;
	}

	/**
	 * Get the root cause of a throwable. This method calls getCause() until the
	 * cause without a cause (the root cause) is found.
	 *
	 * @param throwable The throwable from which to retrieve the root cause
	 * @return The root cause of a throwable
	 */
	public static Throwable getRootCause( Throwable throwable ) {
		Throwable cause = throwable;

		while( cause != null && cause.getCause() != null ) {
			cause = cause.getCause();
		}

		return cause;
	}

	/**
	 * Logs the class loader of the given object at the FINEST level. If the
	 * object is a {@link Class}, its class loader is used; otherwise the
	 * class loader of the object's class is used.
	 *
	 * @param object The object whose class loader should be logged
	 */
	public static void printClassLoader( Object object ) {
		if( object instanceof Class ) {
			log.atFiner().log( "Class loader for %s:%s", getClassName( (Class<?>)object ), ((Class<?>)object).getClassLoader() );
		} else {
			log.atFiner().log( "Class loader for %s:%s", getClassName( object.getClass() ), getClassLoader( object ) );
		}
	}

	/**
	 * Returns the class loader that loaded the class of the given object.
	 *
	 * @param object The object whose class loader should be returned
	 * @return The class loader of the object's class
	 */
	public static ClassLoader getClassLoader( Object object ) {
		return object.getClass().getClassLoader();
	}

	/**
	 * Prints all system properties to standard output, sorted by key, one
	 * {@code key=value} pair per line.
	 */
	public static void printSystemProperties() {
		Properties properties = System.getProperties();
		List<String> keys = new ArrayList<>();
		System.getProperties().keySet().forEach( entry -> keys.add( entry.toString() ) );
		Collections.sort( keys );
		keys.forEach( key -> System.out.println( key + "=" + properties.getProperty( key ) ) );
	}

}
