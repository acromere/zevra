package com.acromere.product;

import com.acromere.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.NonNull;

import java.util.Date;
import java.util.TimeZone;

/**
 * Represents an artifact release, which is a version and a timestamp.
 *
 * <p>A release is identified by its version, with an optional timestamp that
 * distinguishes multiple releases of the same version. Releases are compared
 * first by version, then by timestamp, and can be encoded to and decoded from
 * a single string for storage.</p>
 *
 * @param version the version of the release
 * @param timestamp the time the release was made, or null
 * @author Mark Soderquist
 */
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public record Release(Version version, Date timestamp) implements Comparable<Release> {

	/**
	 * The delimiter separating the version and timestamp in an encoded
	 * release string.
	 */
	private static final String ENCODE_DELIMITER = "  ";

	/**
	 * Create a release with an unknown version and no timestamp.
	 */
	public Release() {
		this( new Version() );
	}

	/**
	 * Create a release with the specified version and no timestamp.
	 *
	 * @param version The version of the release
	 */
	public Release( String version ) {
		this( new Version( version ), null );
	}

	/**
	 * Create a release with the specified version and timestamp.
	 *
	 * @param version The version of the release
	 * @param timestamp The time the release was made, or null
	 */
	public Release( String version, Date timestamp ) {
		this( new Version( version ), timestamp );
	}

	/**
	 * Create a release with the specified version and no timestamp.
	 *
	 * @param version The version of the release
	 */
	public Release( Version version ) {
		this( version, null );
	}

	/**
	 * Compact constructor. The version must not be null.
	 *
	 * @throws NullPointerException If the version is null
	 */
	public Release {
		if( version == null ) throw new NullPointerException( "Version cannot be null." );
	}

	/**
	 * Create a release with the specified version and timestamp string.
	 * <p>
	 * The timestamp string is parsed in the default date format.
	 * </p>
	 *
	 * @param version The version of the release
	 * @param timestamp The time the release was made, in the default date
	 *     format, or null
	 * @return The release with the specified version and timestamp
	 */
	public static Release create( String version, String timestamp ) {
		return new Release( new Version( version ), DateUtil.parse( timestamp, DateUtil.DEFAULT_DATE_FORMAT ) );
	}

	/**
	 * Encode the release as a single string.
	 * <p>
	 * The encoded form is the version, optionally followed by the encode
	 * delimiter and the timestamp in milliseconds since the epoch.
	 * </p>
	 *
	 * @param release The release to encode
	 * @return The encoded release
	 */
	@NonNull
	public static String encode( Release release ) {
		StringBuilder builder = new StringBuilder( release.version.toString() );
		if( release.timestamp != null ) {
			builder.append( ENCODE_DELIMITER );
			builder.append( release.timestamp.getTime() );
		}

		return builder.toString();
	}

	/**
	 * Decode a release from a string created by {@link #encode(Release)}.
	 *
	 * @param release The encoded release, or null
	 * @return The decoded release, or a release with an unknown version if the
	 *     argument is null
	 */
	@NonNull
	public static Release decode( String release ) {
		if( release == null ) return new Release( new Version() );

		int index = release.indexOf( ENCODE_DELIMITER );
		if( index < 0 ) return new Release( new Version( release ) );

		Version version = new Version( release.substring( 0, index ) );
		Date timestamp = new Date( Long.parseLong( release.substring( index + ENCODE_DELIMITER.length() ) ) );
		return new Release( version, timestamp );
	}

	/**
	 * Get the timestamp of the release in the default time zone.
	 *
	 * @return The timestamp of the release in the default date format, or an
	 *     empty string if the release has no timestamp
	 */
	public String getTimestampString() {
		return getTimestampString( DateUtil.DEFAULT_TIME_ZONE );
	}

	/**
	 * Get the timestamp of the release in the specified time zone.
	 *
	 * @param zone The time zone to format the timestamp in
	 * @return The timestamp of the release in the default date format, or an
	 *     empty string if the release has no timestamp
	 */
	public String getTimestampString( TimeZone zone ) {
		if( timestamp == null ) return "";
		return DateUtil.format( timestamp, DateUtil.DEFAULT_DATE_FORMAT, zone );
	}

	/**
	 * Return the release as a string of the form "version  timestamp".
	 *
	 * @return The release as a string
	 */
	@NonNull
	@Override
	public String toString() {
		return format( version.toString() );
	}

	/**
	 * Return the release as a human-friendly string using the default time zone.
	 *
	 * @return The release as a human-friendly string
	 */
	@NonNull
	public String toHumanString() {
		return format( version.toHumanString() );
	}

	/**
	 * Return the release as a human-friendly string in the specified time zone.
	 *
	 * @param zone The time zone to format the timestamp in
	 * @return The release as a human-friendly string
	 */
	@NonNull
	public String toHumanString( TimeZone zone ) {
		return format( version.toHumanString(), zone );
	}

	/**
	 * Two releases are equal if they compare equal with
	 * {@link #compareTo(Release)}.
	 *
	 * @param object The object to compare with
	 * @return {@code true} if the specified object is a release equal to this
	 *     one
	 */
	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof Release that) ) return false;
		return this.compareTo( that ) == 0;
	}

	/**
	 * The hash code is derived from the version and timestamp.
	 *
	 * @return The hash code of this release
	 */
	@Override
	public int hashCode() {
		return version.hashCode() ^ (timestamp == null ? 0 : timestamp.hashCode());
	}

	/**
	 * Compare this release to the specified release.
	 * <p>
	 * Releases are compared by version first, then by timestamp. A release
	 * with no timestamp compares equal to a release with the same version
	 * regardless of the other release's timestamp.
	 * </p>
	 *
	 * @param that The release to compare with
	 * @return A negative integer, zero, or a positive integer as this release
	 *     is less than, equal to, or greater than the specified release
	 */
	@Override
	public int compareTo( Release that ) {
		int result = this.version().compareTo( that.version() );
		if( result != 0 ) return result;

		if( this.timestamp == null || that.timestamp == null ) return 0;
		return this.timestamp.compareTo( that.timestamp );
	}

	private String format( String version ) {
		return format( version, DateUtil.DEFAULT_TIME_ZONE );
	}

	private String format( String version, TimeZone zone ) {
		StringBuilder buffer = new StringBuilder();

		buffer.append( version );
		if( timestamp != null ) {
			buffer.append( "  " );
			buffer.append( getTimestampString( zone ) );
		}

		return buffer.toString();
	}

}
