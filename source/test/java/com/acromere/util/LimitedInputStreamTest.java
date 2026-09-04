package com.acromere.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class LimitedInputStreamTest {

	@Test
	void testReadSingleByteWithLimit() throws IOException {
		byte[] data = new byte[]{ 1, 2, 3, 4, 5 };
		ByteArrayInputStream bais = new ByteArrayInputStream( data );
		LimitedInputStream lis = new LimitedInputStream( bais, 3 );

		assertThat( lis.read() ).isEqualTo( 1 );
		assertThat( lis.read() ).isEqualTo( 2 );
		assertThat( lis.read() ).isEqualTo( 3 );
		assertThat( lis.read() ).isEqualTo( -1 );
		assertThat( lis.read() ).isEqualTo( -1 );
	}

	@Test
	void testReadByteArrayWithLimit() throws IOException {
		byte[] data = new byte[]{ 10, 20, 30, 40, 50 };
		ByteArrayInputStream bais = new ByteArrayInputStream( data );
		LimitedInputStream lis = new LimitedInputStream( bais, 3 );

		byte[] buffer = new byte[ 5 ];
		int readCount = lis.read( buffer );

		assertThat( readCount ).isEqualTo( 3 );
		assertThat( buffer[ 0 ] ).isEqualTo( (byte)10 );
		assertThat( buffer[ 1 ] ).isEqualTo( (byte)20 );
		assertThat( buffer[ 2 ] ).isEqualTo( (byte)30 );
		assertThat( buffer[ 3 ] ).isEqualTo( (byte)0 );
		assertThat( lis.read( buffer ) ).isEqualTo( -1 );
	}

	@Test
	void testReadByteArrayOffsetAndLength() throws IOException {
		byte[] data = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream bais = new ByteArrayInputStream( data );
		LimitedInputStream lis = new LimitedInputStream( bais, 4 );

		byte[] buffer = new byte[ 10 ];
		int readCount1 = lis.read( buffer, 2, 2 );
		assertThat( readCount1 ).isEqualTo( 2 );
		assertThat( buffer[ 2 ] ).isEqualTo( (byte)1 );
		assertThat( buffer[ 3 ] ).isEqualTo( (byte)2 );

		int readCount2 = lis.read( buffer, 4, 5 );
		assertThat( readCount2 ).isEqualTo( 2 ); // only 2 left before limit 4
		assertThat( buffer[ 4 ] ).isEqualTo( (byte)3 );
		assertThat( buffer[ 5 ] ).isEqualTo( (byte)4 );

		assertThat( lis.read( buffer, 0, 1 ) ).isEqualTo( -1 );
	}

	@Test
	void testUnlimitedStream() throws IOException {
		byte[] data = new byte[]{ 1, 2, 3 };
		ByteArrayInputStream bais = new ByteArrayInputStream( data );
		LimitedInputStream lis = new LimitedInputStream( bais );

		byte[] buffer = new byte[ 5 ];
		int readCount = lis.read( buffer, 0, 5 );
		assertThat( readCount ).isEqualTo( 3 );
		assertThat( lis.read() ).isEqualTo( -1 );
	}

	@Test
	void testSkip() throws IOException {
		byte[] data = new byte[]{ 1, 2, 3, 4, 5, 6 };
		ByteArrayInputStream bais = new ByteArrayInputStream( data );
		LimitedInputStream lis = new LimitedInputStream( bais, 4 );

		long skipped = lis.skip( 2 );
		assertThat( skipped ).isEqualTo( 2 );
		assertThat( lis.read() ).isEqualTo( 3 );

		long skippedMore = lis.skip( 5 );
		assertThat( skippedMore ).isEqualTo( 1 ); // only 1 left up to limit
		assertThat( lis.read() ).isEqualTo( -1 );
	}

	@Test
	void testSkipUnlimited() throws IOException {
		byte[] data = new byte[]{ 1, 2, 3, 4, 5 };
		ByteArrayInputStream bais = new ByteArrayInputStream( data );
		LimitedInputStream lis = new LimitedInputStream( bais );

		long skipped = lis.skip( 3 );
		assertThat( skipped ).isEqualTo( 3 );
		assertThat( lis.read() ).isEqualTo( 4 );
	}

	@Test
	void testAvailable() throws IOException {
		byte[] data = new byte[]{ 1, 2, 3, 4 };
		ByteArrayInputStream bais = new ByteArrayInputStream( data );
		LimitedInputStream lis = new LimitedInputStream( bais, 2 );

		assertThat( lis.available() ).isEqualTo( 4 );
		lis.read();
		lis.read();
		assertThat( lis.available() ).isEqualTo( 0 );
	}

	@Test
	void testMarkAndReset() throws IOException {
		byte[] data = new byte[]{ 10, 20, 30, 40 };
		ByteArrayInputStream bais = new ByteArrayInputStream( data );
		LimitedInputStream lis = new LimitedInputStream( bais, 3 );

		assertThat( lis.markSupported() ).isTrue();

		lis.read(); // read 10, position = 1
		lis.mark( 10 );
		assertThat( lis.read() ).isEqualTo( 20 ); // read 20, position = 2

		lis.reset();
		assertThat( lis.read() ).isEqualTo( 20 ); // re-read 20
		assertThat( lis.read() ).isEqualTo( 30 ); // read 30, position = 3
		assertThat( lis.read() ).isEqualTo( -1 );
	}

	@Test
	void testToStringAndClose() throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream( new byte[]{ 1 } );
		LimitedInputStream lis = new LimitedInputStream( bais, 1 );

		assertThat( lis.toString() ).isEqualTo( bais.toString() );
		lis.close();
	}

}
