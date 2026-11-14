package com.acromere.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.LongConsumer;

/**
 * Utility methods to copy, write, and convert data between streams,
 * readers, and writers.
 */
public class IoUtil {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	/**
	 * Copy all data from an input stream to an output stream.
	 *
	 * @param input The stream to copy from
	 * @param output The stream to copy to
	 * @return The number of bytes copied
	 * @throws IOException If an I/O error occurs
	 * @see #copy( InputStream, OutputStream, byte[], LongConsumer )
	 */
	public static long copy( InputStream input, OutputStream output ) throws IOException {
		return copy( input, output, DEFAULT_BUFFER_SIZE );
	}

	/**
	 * Copy all data from an input stream to an output stream, using a
	 * buffer of the given size.
	 *
	 * @param input The stream to copy from
	 * @param output The stream to copy to
	 * @param bufferSize The size of the buffer to use
	 * @return The number of bytes copied
	 * @throws IOException If an I/O error occurs
	 * @see #copy( InputStream, OutputStream, byte[], LongConsumer )
	 */
	public static long copy( InputStream input, OutputStream output, int bufferSize ) throws IOException {
		return copy( input, output, new byte[ bufferSize ] );
	}

	/**
	 * Copy all data from an input stream to an output stream, using the
	 * given buffer.
	 *
	 * @param input The stream to copy from
	 * @param output The stream to copy to
	 * @param buffer The buffer to use
	 * @return The number of bytes copied
	 * @throws IOException If an I/O error occurs
	 * @see #copy( InputStream, OutputStream, byte[], LongConsumer )
	 */
	public static long copy( InputStream input, OutputStream output, byte[] buffer ) throws IOException {
		return copy( input, output, buffer, null );
	}

	/**
	 * Copy all data from an input stream to an output stream, reporting
	 * progress to the given callback.
	 *
	 * @param input The stream to copy from
	 * @param output The stream to copy to
	 * @param progressCallback The callback to report the number of bytes copied to, or null
	 * @return The number of bytes copied
	 * @throws IOException If an I/O error occurs
	 * @see #copy( InputStream, OutputStream, byte[], LongConsumer )
	 */
	public static long copy( InputStream input, OutputStream output, LongConsumer progressCallback ) throws IOException {
		return copy( input, output, DEFAULT_BUFFER_SIZE, progressCallback );
	}

	/**
	 * Copy all data from an input stream to an output stream, using a
	 * buffer of the given size and reporting progress to the given callback.
	 *
	 * @param input The stream to copy from
	 * @param output The stream to copy to
	 * @param bufferSize The size of the buffer to use
	 * @param progressCallback The callback to report the number of bytes copied to, or null
	 * @return The number of bytes copied
	 * @throws IOException If an I/O error occurs
	 * @see #copy( InputStream, OutputStream, byte[], LongConsumer )
	 */
	public static long copy( InputStream input, OutputStream output, int bufferSize, LongConsumer progressCallback ) throws IOException {
		return copy( input, output, new byte[ bufferSize ], progressCallback );
	}

	/**
	 * Copy all data from an input stream to an output stream, using the
	 * given buffer and reporting progress to the given callback.
	 *
	 * @param input The stream to copy from
	 * @param output The stream to copy to
	 * @param buffer The buffer to use
	 * @param progressCallback The callback to report the number of bytes copied to, or null
	 * @return The number of bytes copied
	 * @throws IOException If an I/O error occurs
	 */
	@SuppressWarnings( "Duplicates" )
	public static long copy( InputStream input, OutputStream output, byte[] buffer, LongConsumer progressCallback ) throws IOException {
		int count;
		long total;

		for( total = 0L; -1 != (count = input.read( buffer )); total += count ) {
			output.write( buffer, 0, count );
			if( progressCallback != null ) progressCallback.accept( total );
		}

		return total;
	}

	/**
	 * Copy all data from an input stream to a writer, decoding with the
	 * given encoding name.
	 *
	 * @param input The stream to copy from
	 * @param writer The writer to copy to
	 * @param encoding The name of the encoding to use
	 * @return The number of characters copied
	 * @throws IOException If an I/O error occurs
	 * @see #copy( InputStream, Writer, Charset )
	 */
	public static long copy( InputStream input, Writer writer, String encoding ) throws IOException {
		return copy( input, writer, Charset.forName( encoding ) );
	}

	/**
	 * Copy all data from an input stream to a writer, decoding with the
	 * given encoding.
	 *
	 * @param input The stream to copy from
	 * @param writer The writer to copy to
	 * @param encoding The encoding to use
	 * @return The number of characters copied
	 * @throws IOException If an I/O error occurs
	 */
	public static long copy( InputStream input, Writer writer, Charset encoding ) throws IOException {
		return copy( new InputStreamReader( input, encoding ), writer, DEFAULT_BUFFER_SIZE );
	}

	/**
	 * Copy all data from a reader to a writer.
	 *
	 * @param reader The reader to copy from
	 * @param writer The writer to copy to
	 * @return The number of characters copied
	 * @throws IOException If an I/O error occurs
	 * @see #copy( Reader, Writer, char[], LongConsumer )
	 */
	public static long copy( Reader reader, Writer writer ) throws IOException {
		return copy( reader, writer, new char[ DEFAULT_BUFFER_SIZE ] );
	}

	/**
	 * Copy all data from a reader to a writer, using a buffer of the
	 * given size.
	 *
	 * @param reader The reader to copy from
	 * @param writer The writer to copy to
	 * @param bufferSize The size of the buffer to use
	 * @return The number of characters copied
	 * @throws IOException If an I/O error occurs
	 * @see #copy( Reader, Writer, char[], LongConsumer )
	 */
	public static long copy( Reader reader, Writer writer, int bufferSize ) throws IOException {
		return copy( reader, writer, new char[ bufferSize ] );
	}

	/**
	 * Copy all data from a reader to a writer, using the given buffer.
	 *
	 * @param reader The reader to copy from
	 * @param writer The writer to copy to
	 * @param buffer The buffer to use
	 * @return The number of characters copied
	 * @throws IOException If an I/O error occurs
	 * @see #copy( Reader, Writer, char[], LongConsumer )
	 */
	public static long copy( Reader reader, Writer writer, char[] buffer ) throws IOException {
		return copy( reader, writer, buffer, null );
	}

	/**
	 * Copy all data from a reader to a writer, using the given buffer and
	 * reporting progress to the given callback.
	 *
	 * @param reader The reader to copy from
	 * @param writer The writer to copy to
	 * @param buffer The buffer to use
	 * @param progressCallback The callback to report the number of characters copied to, or null
	 * @return The number of characters copied
	 * @throws IOException If an I/O error occurs
	 */
	@SuppressWarnings( "Duplicates" )
	public static long copy( Reader reader, Writer writer, char[] buffer, LongConsumer progressCallback ) throws IOException {
		int count;
		long total;

		for( total = 0L; -1 != (count = reader.read( buffer )); total += count ) {
			writer.write( buffer, 0, count );
			if( progressCallback != null ) progressCallback.accept( total );
		}

		return total;
	}

	/**
	 * Write a string, encoded with the given encoding name, to an output stream.
	 *
	 * @param data The string to write
	 * @param output The stream to write to
	 * @param encoding The name of the encoding to use
	 * @throws IOException If an I/O error occurs
	 * @see #write( String, OutputStream, Charset )
	 */
	public static void write( String data, OutputStream output, String encoding ) throws IOException {
		write( data, output, Charset.forName( encoding ) );
	}

	/**
	 * Write a string, encoded with the given encoding, to an output stream.
	 *
	 * @param data The string to write
	 * @param output The stream to write to
	 * @param encoding The encoding to use
	 * @throws IOException If an I/O error occurs
	 */
	public static void write( String data, OutputStream output, Charset encoding ) throws IOException {
		write( data.toCharArray(), output, encoding );
	}

	/**
	 * Write a character array, encoded with the given encoding, to an
	 * output stream.
	 *
	 * @param data The characters to write, or null to do nothing
	 * @param output The stream to write to
	 * @param encoding The encoding to use
	 * @throws IOException If an I/O error occurs
	 */
	public static void write( char[] data, OutputStream output, Charset encoding ) throws IOException {
		if( data != null ) output.write( new String( data ).getBytes( encoding ) );
	}

	/**
	 * Read the entire contents of an input stream into a string using
	 * UTF-8 decoding.
	 *
	 * @param input The stream to read
	 * @return The stream contents as a string
	 * @throws IOException If an I/O error occurs
	 */
	public static String toString( InputStream input ) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		input.transferTo( output );
		return output.toString( StandardCharsets.UTF_8 );
	}

	/**
	 * Read the entire contents of an input stream into a string using the
	 * given encoding name.
	 *
	 * @param input The stream to read
	 * @param encoding The name of the encoding to use
	 * @return The stream contents as a string
	 * @throws IOException If an I/O error occurs
	 * @see #toString( InputStream, Charset )
	 */
	public static String toString( InputStream input, String encoding ) throws IOException {
		return toString( input, Charset.forName( encoding ) );
	}

	/**
	 * Read the entire contents of an input stream into a string using the
	 * given encoding.
	 *
	 * @param input The stream to read
	 * @param encoding The encoding to use
	 * @return The stream contents as a string
	 * @throws IOException If an I/O error occurs
	 */
	public static String toString( InputStream input, Charset encoding ) throws IOException {
		try( final StringWriter sw = new StringWriter() ) {
			copy( input, sw, encoding );
			return sw.toString();
		}
	}

	/**
	 * Read the entire contents of a reader into a string.
	 *
	 * @param reader The reader to read
	 * @return The reader contents as a string
	 * @throws IOException If an I/O error occurs
	 */
	public static String toString( Reader reader ) throws IOException {
		StringWriter writer = new StringWriter();
		reader.transferTo( writer );
		return writer.toString();
	}

}
