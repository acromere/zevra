package com.acromere.util;

import java.io.InputStream;

/**
 * Strategy for computing a hash of a stream of data.
 *
 * <p>Implementations read the entire input stream and return a hash value,
 * typically encoded as a hexadecimal string. Useful for caching, change
 * detection, and deduplication of file contents.</p>
 */
public interface HashStrategy {

	/**
	 * Computes the hash of the given input stream.
	 *
	 * <p>The stream is fully consumed by this method and cannot be reused
	 * afterwards.</p>
	 *
	 * @param input The stream of data to hash, must not be {@code null}
	 * @return The hash value of the data, or {@code null} if an error occurs
	 */
	String hash( InputStream input );

}
