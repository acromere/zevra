package com.acromere.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

/**
 * Utility class for indenting and unindenting blocks of text.
 *
 * <p>Line terminators are preserved as-is; see {@link LineParser} for the
 * supported terminator styles. The default indent string is a single tab
 * ({@link #DEFAULT_INDENT_STRING}).</p>
 */
public class Indenter {

	/** The default indent string, a single tab character. */
	public static final String DEFAULT_INDENT_STRING = "\t";

	/**
	 * Creates the default single indent.
	 *
	 * @return The default indent string
	 */
	public static String createIndent() {
		return DEFAULT_INDENT_STRING;
	}

	/**
	 * Creates an indent consisting of the default indent string repeated
	 * the given number of times.
	 *
	 * @param count The number of times to repeat the indent
	 * @return The indent string
	 */
	public static String createIndent( int count ) {
		return createIndent( count, DEFAULT_INDENT_STRING );
	}

	/**
	 * Creates an indent string by repeating the given indent the given
	 * number of times.
	 *
	 * @param count The number of times to repeat the indent
	 * @param indent The indent string to repeat
	 * @return The repeated indent string, or an empty string if
	 * {@code count} is less than 1
	 */
	public static String createIndent( int count, String indent ) {
		if( count == 1 ) return indent;

		int size = count * indent.length();
		if( size < 1 ) return "";

		int length = indent.length();
		char[] buffer = new char[ size ];
		char[] chars = indent.toCharArray();
		for( int index = 0; index < count; index++ ) {
			System.arraycopy( chars, 0, buffer, index * length, length );
		}

		return new String( buffer );
	}

	/**
	 * Writes the default single indent to the given writer.
	 *
	 * @param writer The writer to write to
	 * @throws IOException If an I/O error occurs
	 */
	public static void writeIndent( Writer writer ) throws IOException {
		writer.write( DEFAULT_INDENT_STRING );
	}

	/**
	 * Writes the default indent repeated the given number of times to the
	 * given writer.
	 *
	 * @param writer The writer to write to
	 * @param count The number of times to repeat the indent
	 * @throws IOException If an I/O error occurs
	 */
	public static void writeIndent( Writer writer, int count ) throws IOException {
		writeIndent( writer, count, DEFAULT_INDENT_STRING );
	}

	/**
	 * Writes the given indent to the given writer.
	 *
	 * @param writer The writer to write to
	 * @param indent The indent string to write
	 * @throws IOException If an I/O error occurs
	 */
	public static void writeIndent( Writer writer, String indent ) throws IOException {
		writeIndent( writer, 1, indent );
	}

	/**
	 * Writes the given indent repeated the given number of times to the
	 * given writer.
	 *
	 * @param writer The writer to write to
	 * @param count The number of times to repeat the indent
	 * @param indent The indent string to repeat
	 * @throws IOException If an I/O error occurs
	 */
	public static void writeIndent( Writer writer, int count, String indent ) throws IOException {
		if( count < 1 ) return;
		writer.write( createIndent( count, indent ) );
	}

	/**
	 * Indents every line of the given content by one level using the
	 * default indent string.
	 *
	 * @param content The content to indent
	 * @return The indented content, or {@code null} if the content is {@code null}
	 */
	public static String indent( String content ) {
		return indent( content, 1 );
	}

	/**
	 * Indents every line of the given content by the given number of
	 * levels using the default indent string.
	 *
	 * @param content The content to indent
	 * @param size The number of indent levels
	 * @return The indented content, or {@code null} if the content is {@code null}
	 */
	public static String indent( String content, int size ) {
		return indent( content, size, DEFAULT_INDENT_STRING );
	}

	/**
	 * Indents every line of the given content by one level using the
	 * given indent string.
	 *
	 * @param content The content to indent
	 * @param indent The indent string to use
	 * @return The indented content, or {@code null} if the content is {@code null}
	 */
	public static String indent( String content, String indent ) {
		return indent( content, 1, indent );
	}

	/**
	 * Indents every line of the given content by the given number of
	 * levels using the given indent string.
	 *
	 * @param content The content to indent
	 * @param size The number of indent levels
	 * @param indent The indent string to use
	 * @return The indented content, or {@code null} if the content is {@code null}
	 */
	public static String indent( String content, int size, String indent ) {
		if( content == null ) return null;

		LineParser parser = new LineParser( content );
		StringBuilder builder = new StringBuilder();
		String text = createIndent( size, indent );

		String line;
		while( (line = parser.next()) != null ) {
			builder.append( text );
			builder.append( line );
			builder.append( parser.getTerminator() );
		}

		return builder.toString();
	}

	/**
	 * Checks whether every line of the content starts with the default
	 * single indent.
	 *
	 * @param content The content to check
	 * @return {@code true} if the content can be unindented
	 * @see #canUnindent(String, int, String, boolean)
	 */
	public static boolean canUnindent( String content ) {
		return canUnindent( content, 1 );
	}

	/**
	 * Checks whether every non-blank line of the content starts with the
	 * default single indent.
	 *
	 * @param content The content to check
	 * @param ignoreBlanks Whether blank lines are ignored when checking
	 * @return {@code true} if the content can be unindented
	 * @see #canUnindent(String, int, String, boolean)
	 */
	public static boolean canUnindent( String content, boolean ignoreBlanks ) {
		return canUnindent( content, 1, ignoreBlanks );
	}

	/**
	 * Checks whether every line of the content starts with the default
	 * indent repeated the given number of times.
	 *
	 * @param content The content to check
	 * @param size The number of indent levels
	 * @return {@code true} if the content can be unindented
	 * @see #canUnindent(String, int, String, boolean)
	 */
	public static boolean canUnindent( String content, int size ) {
		return canUnindent( content, size, DEFAULT_INDENT_STRING );
	}

	/**
	 * Checks whether every line of the content starts with the default
	 * indent repeated the given number of times.
	 *
	 * @param content The content to check
	 * @param size The number of indent levels
	 * @param ignoreBlanks Whether blank lines are ignored when checking
	 * @return {@code true} if the content can be unindented
	 * @see #canUnindent(String, int, String, boolean)
	 */
	public static boolean canUnindent( String content, int size, boolean ignoreBlanks ) {
		return canUnindent( content, size, DEFAULT_INDENT_STRING, ignoreBlanks );
	}

	/**
	 * Checks whether every line of the content starts with the given indent
	 * repeated the given number of times.
	 *
	 * @param content The content to check
	 * @param size The number of indent levels
	 * @param indent The indent string to check for
	 * @return {@code true} if the content can be unindented
	 * @see #canUnindent(String, int, String, boolean)
	 */
	public static boolean canUnindent( String content, int size, String indent ) {
		return canUnindent( content, size, indent, false );
	}

	/**
	 * Checks whether every line of the content starts with the given indent
	 * repeated the given number of times, so that the content can be safely
	 * unindented with {@link #unindent(String, int, String) unindent()}.
	 *
	 * @param content The content to check
	 * @param size The number of indent levels
	 * @param indent The indent string to check for
	 * @param ignoreBlanks Whether blank lines are ignored when checking;
	 * when {@code false}, a blank line causes the method to return
	 * {@code false}
	 * @return {@code true} if all lines can be unindented, {@code false}
	 * otherwise, including when the content is {@code null}
	 */
	public static boolean canUnindent( String content, int size, String indent, boolean ignoreBlanks ) {
		if( content == null ) return false;

		LineParser parser = new LineParser( content );
		String text = createIndent( size, indent );

		String line;
		while( (line = parser.next()) != null ) {
			if( ignoreBlanks && line.isEmpty() ) continue;
			if( !line.startsWith( text ) ) return false;
		}

		return true;
	}

	/**
	 * Removes one level of the default indent from the start of each line
	 * of the content.
	 *
	 * @param content The content to unindent
	 * @return The unindented content, or {@code null} if the content is
	 * {@code null}
	 * @see #canUnindent(String)
	 */
	public static String unindent( String content ) {
		return unindent( content, 1 );
	}

	/**
	 * Removes the given number of levels of the default indent from the
	 * start of each line of the content.
	 *
	 * @param content The content to unindent
	 * @param size The number of indent levels to remove
	 * @return The unindented content, or {@code null} if the content is
	 * {@code null}
	 * @see #canUnindent(String, int)
	 */
	public static String unindent( String content, int size ) {
		return unindent( content, size, DEFAULT_INDENT_STRING );
	}

	/**
	 * Removes the given indent, repeated the given number of times, from
	 * the start of each line of the content. Lines that do not start with
	 * the indent have their content dropped, leaving only the line
	 * terminator. Use {@link #canUnindent(String, int, String, boolean)
	 * canUnindent()} first to ensure the content can be unindented without
	 * loss.
	 *
	 * @param content The content to unindent
	 * @param size The number of indent levels to remove
	 * @param indent The indent string to remove
	 * @return The unindented content, or {@code null} if the content is
	 * {@code null}
	 * @see #canUnindent(String, int, String, boolean)
	 */
	public static String unindent( String content, int size, String indent ) {
		if( content == null ) return null;

		LineParser parser = new LineParser( content );
		StringBuilder builder = new StringBuilder();
		String text = createIndent( size, indent );

		String line;
		while( (line = parser.next()) != null ) {
			if( line.startsWith( text ) ) builder.append( line.substring( text.length() ) );
			builder.append( parser.getTerminator() );
		}

		return builder.toString();
	}

	/**
	 * Removes all leading and trailing characters of the content that are
	 * contained in the given set of trim characters.
	 *
	 * @param content The content to trim
	 * @param trimChars The set of characters to trim from the start and end
	 * @return The trimmed content, or the original content if it is
	 * {@code null} or empty
	 */
	public static String trim( String content, String trimChars ) {
		if( content == null || content.isEmpty() ) return content;

		int length = content.length();
		int start = 0;
		int end = length - 1;
		char c;
		char[] value = new char[ length ];
		char[] trims = new char[ trimChars.length() ];

		content.getChars( 0, value.length, value, 0 );
		trimChars.getChars( 0, trims.length, trims, 0 );

		boolean left = true;
		while( left & start <= end ) {
			c = value[ start ];
			boolean found = false;
			for( char trim : trims ) {
				if( c == trim ) {
					found = true;
					break;
				}
			}
			left = found;
			if( found ) start++;
		}

		boolean right = true;
		while( right & end >= start ) {
			c = value[ end ];
			boolean found = false;
			for( char trim : trims ) {
				if( c == trim ) {
					found = true;
					break;
				}
			}
			right = found;
			if( found ) end--;
		}

		String result = content;
		if( (start > 0) || (end < length - 1) ) {
			result = new String( value, start, end - start + 1 );
		}

		return result;
	}

	/**
	 * Trims the given characters from the start and end of each line of the
	 * content. When the trim characters include the newline and whitespace
	 * characters, leading and trailing blank lines are effectively removed
	 * and the surrounding padding of every line is collapsed.
	 *
	 * @param content The content to trim
	 * @param trimChars The set of characters to trim from each line
	 * @return The trimmed content, or the original content if it is
	 * {@code null} or empty
	 */
	public static String trimLines( String content, String trimChars ) {
		if( content == null || content.isEmpty() ) return content;

		StringBuilder builder = new StringBuilder();
		StringReader stringReader = new StringReader( content );
		BufferedReader bufferedReader = new BufferedReader( stringReader );

		int lineNumber = 0;
		StringBuilder lineBuilder;
		try {
			lineNumber++;
			String line = bufferedReader.readLine();
			while( line != null && line.isEmpty() ) {
				builder.append( trim( "\n", trimChars ) );
				line = bufferedReader.readLine();
			}
			if( line != null ) {
				while( line != null ) {
					lineBuilder = new StringBuilder( line );
					lineBuilder.append( "\n" );
					builder.append( trim( lineBuilder.toString(), trimChars ) );
					line = bufferedReader.readLine();
				}
			}
		} catch( IOException exception ) {
			throw new RuntimeException( "Could not unindent at line: " + lineNumber );
		}

		return builder.toString();
	}

}
