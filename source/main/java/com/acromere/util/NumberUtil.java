package com.acromere.util;

/**
 * Utility class for parsing numbers from arbitrary objects without throwing
 * exceptions.
 *
 * <p>All methods convert the given object to its string form and attempt to
 * parse it as a number. If the object is {@code null}, not a valid number,
 * or the parse fails for any reason, the supplied default value is returned
 * instead.</p>
 */
public class NumberUtil {

	/**
	 * Parses an object as an {@code int}.
	 *
	 * @param object The object to parse, converted via {@link String#valueOf(Object)}
	 * @param defaultValue The value to return if parsing fails
	 * @return The parsed value, or {@code defaultValue} if parsing fails
	 */
	public static int parseInt( Object object, int defaultValue ) {
		try {
			return Integer.parseInt( String.valueOf( object ) );
		} catch( NumberFormatException exception ) {
			return defaultValue;
		}
	}

	/**
	 * Parses an object as a {@code long}.
	 *
	 * @param object The object to parse, converted via {@link String#valueOf(Object)}
	 * @param defaultValue The value to return if parsing fails
	 * @return The parsed value, or {@code defaultValue} if parsing fails
	 */
	public static long parseLong( Object object, long defaultValue ) {
		try {
			return Long.parseLong( String.valueOf( object ) );
		} catch( NumberFormatException exception ) {
			return defaultValue;
		}
	}

	/**
	 * Parses an object as a {@code float}.
	 *
	 * @param object The object to parse, converted via {@link String#valueOf(Object)}
	 * @param defaultValue The value to return if parsing fails
	 * @return The parsed value, or {@code defaultValue} if parsing fails
	 */
	public static float parseFloat( Object object, float defaultValue ) {
		try {
			return Float.parseFloat( String.valueOf( object ) );
		} catch( NumberFormatException exception ) {
			return defaultValue;
		}
	}

	/**
	 * Parses an object as a {@code double}.
	 *
	 * @param object The object to parse, converted via {@link String#valueOf(Object)}
	 * @param defaultValue The value to return if parsing fails
	 * @return The parsed value, or {@code defaultValue} if parsing fails
	 */
	public static double parseDouble( Object object, double defaultValue ) {
		try {
			return Double.parseDouble( String.valueOf( object ) );
		} catch( NumberFormatException exception ) {
			return defaultValue;
		}
	}

}
