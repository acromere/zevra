package com.acromere.util;

import lombok.CustomLog;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for computing message digests (hashes) of data.
 *
 * <p>Provides convenient overloads for hashing strings, byte arrays, files,
 * and input streams using a pluggable {@link HashStrategy}. All digests are
 * returned as lowercase hex-encoded strings via
 * {@link TextUtil#toHexEncodedString(byte[]) toHexEncodedString()}.</p>
 */
@CustomLog
public class HashUtil {

	/** Hash strategy using the SHA3-256 algorithm. */
	public static final HashStrategy SHA3 = new DigestStrategy( new MessageDigestWrapper( "SHA3-256" ) );

	/** Hash strategy using the SHA-256 algorithm. */
	public static final HashStrategy SHA2 = new DigestStrategy( new MessageDigestWrapper( "SHA-256" ) );

	/** Hash strategy using the SHA-1 algorithm. */
	public static final HashStrategy SHA1 = new DigestStrategy( new MessageDigestWrapper( "SHA-1" ) );

	/** The default hash strategy used by the single-argument hash methods ({@link #SHA1 SHA-1}). */
	public static final HashStrategy DEFAULT_STRATEGY = SHA1;

	/**
	 * Computes the hash of a string using the default hash strategy.
	 *
	 * @param text The string to hash
	 * @return The hex-encoded hash, or {@code null} if the text is {@code null}
	 */
	public static String hash( String text ) {
		if( text == null ) return null;
		return hash( text, DEFAULT_STRATEGY );
	}

	/**
	 * Computes the hash of a string using the given strategy. The string is
	 * encoded to bytes using UTF-8.
	 *
	 * @param text The string to hash
	 * @param strategy The hash strategy to use
	 * @return The hex-encoded hash
	 */
	public static String hash( String text, HashStrategy strategy ) {
		return hash( text.getBytes( TextUtil.CHARSET ), strategy );
	}

	/**
	 * Computes the hash of a byte array using the default hash strategy.
	 *
	 * @param bytes The bytes to hash
	 * @return The hex-encoded hash, or {@code null} if the array is {@code null}
	 */
	public static String hash( byte[] bytes ) {
		if( bytes == null ) return null;
		return hash( new ByteArrayInputStream( bytes ), DEFAULT_STRATEGY );
	}

	/**
	 * Computes the hash of a byte array using the given strategy.
	 *
	 * @param bytes The bytes to hash
	 * @param strategy The hash strategy to use
	 * @return The hex-encoded hash
	 */
	public static String hash( byte[] bytes, HashStrategy strategy ) {
		return hash( new ByteArrayInputStream( bytes ), strategy );
	}

	/**
	 * Computes the hash of a file using the default hash strategy.
	 *
	 * @param path The path of the file to hash
	 * @return The hex-encoded hash
	 */
	public static String hash( Path path ) {
		return hash( path.toFile(), DEFAULT_STRATEGY );
	}

	/**
	 * Computes the hash of a file using the given strategy.
	 *
	 * @param path The path of the file to hash
	 * @param strategy The hash strategy to use
	 * @return The hex-encoded hash
	 */
	public static String hash( Path path, HashStrategy strategy ) {
		return hash( path.toFile(), strategy );
	}

	/**
	 * Computes the hash of a file using the default hash strategy.
	 *
	 * @param file The file to hash
	 * @return The hex-encoded hash
	 */
	public static String hash( File file ) {
		return hash( file, DEFAULT_STRATEGY );
	}

	/**
	 * Computes the hash of a file using the given strategy.
	 *
	 * @param file The file to hash
	 * @param strategy The hash strategy to use
	 * @return The hex-encoded hash, or {@code null} if the file is
	 * {@code null}, does not exist, is not a regular file, or cannot be read
	 */
	public static String hash( File file, HashStrategy strategy ) {
		if( file == null || !file.exists() || !file.isFile() ) return null;

		try( FileInputStream input = new FileInputStream( file ) ) {
			return hash( input, strategy );
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error reading file %s", file );
			return null;
		}
	}

	/**
	 * Computes the hash of an input stream using the default hash strategy.
	 *
	 * @param input The stream to hash
	 * @return The hex-encoded hash
	 */
	public static String hash( InputStream input ) {
		return hash( input, DEFAULT_STRATEGY );
	}

	/**
	 * Computes the hash of an input stream using the given strategy.
	 *
	 * <p>The stream is fully consumed by this method.</p>
	 *
	 * @param input The stream to hash
	 * @param strategy The hash strategy to use
	 * @return The hex-encoded hash, or {@code null} if the stream is {@code null}
	 */
	public static String hash( InputStream input, HashStrategy strategy ) {
		if( input == null ) return null;
		return strategy.hash( input );
	}

	/**
	 * Abstraction over {@link MessageDigest} that allows the underlying digest
	 * to be swapped or mocked.
	 */
	private interface DigestWrapper {

		/** Resets the digest for reuse. */
		void reset();

		/**
		 * Updates the digest with the given bytes.
		 *
		 * @param input The byte array containing the data
		 * @param offset The start offset of the data within the array
		 * @param len The number of bytes to process
		 */
		void update( byte[] input, int offset, int len );

		/**
		 * Finalizes the digest and returns the resulting hash bytes.
		 *
		 * @return The hash bytes
		 */
		byte[] digest();

	}

	/**
	 * A {@link HashStrategy} backed by a {@link DigestWrapper}. Reads the
	 * input stream in 4096-byte chunks and feeds it to the underlying digest.
	 *
	 * @param digest The underlying message digest wrapper
	 */
	private record DigestStrategy(DigestWrapper digest) implements HashStrategy {

		@Override
		public String hash( InputStream input ) {
			if( input == null ) return null;

			byte[] buffer = new byte[ 4096 ];
			digest.reset();

			int count;
			try {
				while( (count = input.read( buffer )) > -1 ) {
					digest.update( buffer, 0, count );
				}
			} catch( IOException exception ) {
				log.atSevere().withCause( exception ).log( "Error reading stream" );
				return null;
			}

			return TextUtil.toHexEncodedString( digest.digest() );
		}

	}

	/**
	 * A {@link DigestWrapper} backed by a JDK {@link MessageDigest} instance.
	 */
	private static final class MessageDigestWrapper implements DigestWrapper {

		private final MessageDigest digest;

		/**
		 * Creates a wrapper for the given digest algorithm.
		 *
		 * @param algorithm The name of the digest algorithm, e.g. {@code "SHA-256"}
		 */
		public MessageDigestWrapper( String algorithm ) {
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance( algorithm );
			} catch( NoSuchAlgorithmException exception ) {
				exception.printStackTrace( System.err );
				log.atSevere().withCause( exception ).log( "Error creating digest" );
			}
			this.digest = digest;
		}

		@Override
		public void reset() {
			digest.reset();
		}

		@Override
		public void update( byte[] input, int offset, int length ) {
			digest.update( input, offset, length );
		}

		@Override
		public byte[] digest() {
			return digest.digest();
		}

	}

}
