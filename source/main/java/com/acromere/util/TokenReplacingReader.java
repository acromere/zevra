package com.acromere.util;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Map;

/**
 * Replaces tokens marked by ${tokenName} with values provided in a value map.
 * <p>
 * This class is not thread-safe.
 * </p>
 * <p>
 * For example, using the reader with the following string:
 * <br/>
 * <code>&quot;Hello ${name}!&quot;</code>
 * <br/>
 * ...and the following value map:
 * <table>
 *   <tr>
 *     <th>Name</th>
 *     <th>Value</th>
 *   </tr>
 *   <tr>
 *     <td>name</td>
 *     <td>World</td>
 *   </tr>
 * </table>
 * Would result in: <code>Hello World!</code>
 * </p>
 * <p>
 * Code used and modified from: <a href="https://github.com/jjenkov/TokenReplacingReader">TokenReplacingReader by JJenkov</a>
 * </p>
 */
public class TokenReplacingReader extends Reader {

	/**
	 * The source reader, wrapped in a pushback reader so characters can be
	 * unread while a token name is being read.
	 */
	private final PushbackReader source;

	/**
	 * The map of token names to replacement values.
	 */
	private final Map<String, String> values;

	/**
	 * The name of the token currently being read.
	 */
	private final StringBuilder tokenName;

	/**
	 * The index of the next character to return from the pending
	 * replacement value.
	 */
	private int tokenValueIndex;

	/**
	 * The pending replacement value whose characters have not yet been
	 * returned by the read methods.
	 */
	private String tokenValue;

	/**
	 * Creates a new TokenReplacingReader that replaces tokens in the
	 * specified source with values from the specified map.
	 *
	 * @param source The reader to read the input from
	 * @param values The map of token names to replacement values, or null
	 * to treat every token as having no replacement value
	 * @throws NullPointerException if the source reader is null
	 */
	public TokenReplacingReader( Reader source, Map<String, String> values ) {
		this.source = new PushbackReader( source );
		this.tokenName = new StringBuilder();
		this.values = values == null ? Map.of() : values;
	}

	/**
	 * Reads the next character of input from the stream. If the character
	 * completes a token, the replacement value of the token is returned one
	 * character at a time. Tokens without a replacement value are returned
	 * unchanged, and tokens with an empty replacement value are removed
	 * from the stream. This method blocks until a character of input is
	 * available, the end of the stream is reached, or an I/O error occurs.
	 *
	 * @return The next character of input, or -1 if the end of the stream
	 * has been reached
	 * @throws IOException if an I/O error occurs
	 */
	public int read() throws IOException {
		// If there is replacement value continue sending it
		if( this.tokenValue != null ) {
			// Not at the end of the replacement value
			if( this.tokenValueIndex < this.tokenValue.length() ) {
				return this.tokenValue.charAt( this.tokenValueIndex++ );
			}

			// At the end of the replacement value
			if( this.tokenValueIndex == this.tokenValue.length() ) {
				this.tokenValue = null;
				this.tokenValueIndex = 0;
			}
		}

		// Read the next character from the source
		int data = this.source.read();

		// If not the start of a token, just return the character
		if( data != '$' ) return data;

		// Read the next character from the source
		data = this.source.read();

		// If not the start of a token, return the prior character
		if( data != '{' ) {
			this.source.unread( data );
			return '$';
		}

		// Clear the token name
		this.tokenName.delete( 0, this.tokenName.length() );

		// Read the token name
		data = this.source.read();
		while( data != '}' ) {
			this.tokenName.append( (char)data );
			data = this.source.read();
		}

		// Get the replacement value
		this.tokenValue = this.values.get( this.tokenName.toString() );

		// Check for null replacement values
		if( this.tokenValue == null ) {
			this.tokenValue = "${" + this.tokenName + "}";
		}

		// Check for empty replacement values
		if( this.tokenValue.isEmpty() ) {
			return read();
		}

		// Return the replacement value's next character
		return this.tokenValue.charAt( this.tokenValueIndex++ );
	}

	/**
	 * Reads characters from the stream into the specified buffer.
	 *
	 * @param buffer The buffer to read the characters into
	 * @return The number of characters read, or -1 if the end of the stream
	 * has been reached
	 * @throws IOException if an I/O error occurs
	 * @see #read( char[], int, int )
	 */
	public int read( char @NonNull [] buffer ) throws IOException {
		return read( buffer, 0, buffer.length );
	}

	/**
	 * Reads up to length characters from the stream into the specified
	 * buffer, starting at the specified offset. This method blocks until
	 * input data is available, the end of the stream is reached, or an I/O
	 * error occurs.
	 *
	 * @param buffer The buffer to read the characters into
	 * @param offset The offset in the buffer at which to start storing
	 * characters
	 * @param length The maximum number of characters to read
	 * @return The number of characters read, or -1 if the end of the stream
	 * has been reached before any characters are read
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public int read( char @NonNull [] buffer, int offset, int length ) throws IOException {
		int result = 0;

		for( int i = 0; i < length; i++ ) {
			int next = read();
			if( next == -1 ) {
				if( result == 0 ) result = -1;
				break;
			}
			result = i + 1;
			buffer[ offset + i ] = (char)next;
		}

		return result;
	}

	/**
	 * Reads characters into the specified buffer. This method is not
	 * supported.
	 *
	 * @param target The buffer to read the characters into
	 * @throws UnsupportedOperationException always
	 */
	public int read( @NonNull CharBuffer target ) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks if the reader is ready to be read.
	 *
	 * @return true if the next read is guaranteed not to block on input
	 * @throws IOException if an I/O error occurs
	 */
	public boolean ready() throws IOException {
		return this.source.ready();
	}

	/**
	 * Closes the reader and releases any resources associated with it.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		this.source.close();
	}

	/**
	 * Marks the present position in the stream. This method is not
	 * supported.
	 *
	 * @param readAheadLimit The maximum number of characters that may be
	 * read before the mark is invalidated
	 * @throws UnsupportedOperationException always
	 */
	public void mark( int readAheadLimit ) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Skips over a number of characters. This method is not supported.
	 *
	 * @param n The number of characters to skip
	 * @throws UnsupportedOperationException always
	 */
	public long skip( long n ) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Resets the stream to the last marked position. This method is not
	 * supported.
	 *
	 * @throws UnsupportedOperationException always
	 */
	public void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

}
