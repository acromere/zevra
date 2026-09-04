package com.acromere.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class IoUtilTest {

	@Test
	void testConstructor() {
		assertThat( new IoUtil() ).isNotNull();
	}

	@Test
	void testCopyInputStreamToOutputStream() throws IOException {
		byte[] source = "Hello, World!".getBytes( StandardCharsets.UTF_8 );
		ByteArrayInputStream input = new ByteArrayInputStream( source );
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		long copied = IoUtil.copy( input, output );

		assertThat( copied ).isEqualTo( source.length );
		assertThat( output.toByteArray() ).isEqualTo( source );
	}

	@Test
	void testCopyInputStreamToOutputStreamWithBufferSize() throws IOException {
		byte[] source = "Testing copy with buffer size".getBytes( StandardCharsets.UTF_8 );
		ByteArrayInputStream input = new ByteArrayInputStream( source );
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		long copied = IoUtil.copy( input, output, 4 );

		assertThat( copied ).isEqualTo( source.length );
		assertThat( output.toByteArray() ).isEqualTo( source );
	}

	@Test
	void testCopyInputStreamToOutputStreamWithBuffer() throws IOException {
		byte[] source = "Testing copy with explicit buffer array".getBytes( StandardCharsets.UTF_8 );
		ByteArrayInputStream input = new ByteArrayInputStream( source );
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		long copied = IoUtil.copy( input, output, new byte[ 8 ] );

		assertThat( copied ).isEqualTo( source.length );
		assertThat( output.toByteArray() ).isEqualTo( source );
	}

	@Test
	void testCopyInputStreamToOutputStreamWithCallback() throws IOException {
		byte[] source = "Testing progress callback".getBytes( StandardCharsets.UTF_8 );
		ByteArrayInputStream input = new ByteArrayInputStream( source );
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		AtomicLong callbackCount = new AtomicLong();

		long copied = IoUtil.copy( input, output, 5, callbackCount::set );

		assertThat( copied ).isEqualTo( source.length );
		assertThat( output.toByteArray() ).isEqualTo( source );
		assertThat( callbackCount.get() ).isGreaterThan( 0L );
	}

	@Test
	void testCopyInputStreamToOutputStreamWithCallbackDefaultBuffer() throws IOException {
		byte[] source = "Testing progress callback default buffer".getBytes( StandardCharsets.UTF_8 );
		ByteArrayInputStream input = new ByteArrayInputStream( source );
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		AtomicLong callbackCount = new AtomicLong();

		long copied = IoUtil.copy( input, output, callbackCount::set );

		assertThat( copied ).isEqualTo( source.length );
		assertThat( output.toByteArray() ).isEqualTo( source );
	}

	@Test
	void testCopyInputStreamToWriterWithCharsetName() throws IOException {
		String text = "Copy to writer string encoding";
		byte[] source = text.getBytes( StandardCharsets.UTF_8 );
		ByteArrayInputStream input = new ByteArrayInputStream( source );
		StringWriter writer = new StringWriter();

		long copied = IoUtil.copy( input, writer, "UTF-8" );

		assertThat( copied ).isEqualTo( text.length() );
		assertThat( writer.toString() ).isEqualTo( text );
	}

	@Test
	void testCopyInputStreamToWriterWithCharset() throws IOException {
		String text = "Copy to writer Charset";
		byte[] source = text.getBytes( StandardCharsets.UTF_8 );
		ByteArrayInputStream input = new ByteArrayInputStream( source );
		StringWriter writer = new StringWriter();

		long copied = IoUtil.copy( input, writer, StandardCharsets.UTF_8 );

		assertThat( copied ).isEqualTo( text.length() );
		assertThat( writer.toString() ).isEqualTo( text );
	}

	@Test
	void testCopyReaderToWriter() throws IOException {
		String text = "Testing Reader to Writer copy";
		StringReader reader = new StringReader( text );
		StringWriter writer = new StringWriter();

		long copied = IoUtil.copy( reader, writer );

		assertThat( copied ).isEqualTo( text.length() );
		assertThat( writer.toString() ).isEqualTo( text );
	}

	@Test
	void testCopyReaderToWriterWithBufferSize() throws IOException {
		String text = "Testing Reader to Writer copy with buffer size";
		StringReader reader = new StringReader( text );
		StringWriter writer = new StringWriter();

		long copied = IoUtil.copy( reader, writer, 4 );

		assertThat( copied ).isEqualTo( text.length() );
		assertThat( writer.toString() ).isEqualTo( text );
	}

	@Test
	void testCopyReaderToWriterWithBuffer() throws IOException {
		String text = "Testing Reader to Writer copy with char buffer";
		StringReader reader = new StringReader( text );
		StringWriter writer = new StringWriter();

		long copied = IoUtil.copy( reader, writer, new char[ 8 ] );

		assertThat( copied ).isEqualTo( text.length() );
		assertThat( writer.toString() ).isEqualTo( text );
	}

	@Test
	void testCopyReaderToWriterWithCallback() throws IOException {
		String text = "Testing Reader to Writer copy with callback";
		StringReader reader = new StringReader( text );
		StringWriter writer = new StringWriter();
		AtomicLong progress = new AtomicLong();

		long copied = IoUtil.copy( reader, writer, new char[ 4 ], progress::set );

		assertThat( copied ).isEqualTo( text.length() );
		assertThat( writer.toString() ).isEqualTo( text );
		assertThat( progress.get() ).isGreaterThan( 0L );
	}

	@Test
	void testWriteStringToOutputStreamWithCharsetName() throws IOException {
		String data = "Writing string using encoding name";
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		IoUtil.write( data, output, "UTF-8" );

		assertThat( output.toString( StandardCharsets.UTF_8 ) ).isEqualTo( data );
	}

	@Test
	void testWriteStringToOutputStreamWithCharset() throws IOException {
		String data = "Writing string using Charset";
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		IoUtil.write( data, output, StandardCharsets.UTF_8 );

		assertThat( output.toString( StandardCharsets.UTF_8 ) ).isEqualTo( data );
	}

	@Test
	void testWriteCharArrayToOutputStream() throws IOException {
		String data = "Writing char array";
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		IoUtil.write( data.toCharArray(), output, StandardCharsets.UTF_8 );

		assertThat( output.toString( StandardCharsets.UTF_8 ) ).isEqualTo( data );
	}

	@Test
	void testWriteNullCharArrayToOutputStream() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		IoUtil.write( (char[])null, output, StandardCharsets.UTF_8 );

		assertThat( output.size() ).isZero();
	}

	@Test
	void testToStringInputStream() throws IOException {
		String data = "Convert InputStream to String UTF-8 default";
		ByteArrayInputStream input = new ByteArrayInputStream( data.getBytes( StandardCharsets.UTF_8 ) );

		String result = IoUtil.toString( input );

		assertThat( result ).isEqualTo( data );
	}

	@Test
	void testToStringInputStreamWithCharsetName() throws IOException {
		String data = "Convert InputStream to String with charset name";
		ByteArrayInputStream input = new ByteArrayInputStream( data.getBytes( StandardCharsets.UTF_8 ) );

		String result = IoUtil.toString( input, "UTF-8" );

		assertThat( result ).isEqualTo( data );
	}

	@Test
	void testToStringInputStreamWithCharset() throws IOException {
		String data = "Convert InputStream to String with Charset object";
		ByteArrayInputStream input = new ByteArrayInputStream( data.getBytes( StandardCharsets.UTF_8 ) );

		String result = IoUtil.toString( input, StandardCharsets.UTF_8 );

		assertThat( result ).isEqualTo( data );
	}

	@Test
	void testToStringReader() throws IOException {
		String data = "Convert Reader to String";
		StringReader reader = new StringReader( data );

		String result = IoUtil.toString( reader );

		assertThat( result ).isEqualTo( data );
	}

}
