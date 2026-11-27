package com.acromere.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A delimited set of numbers and letters that represents the version of an
 * item.
 *
 * <p>A version can represent version patterns such as
 * <a href="https://semver.org/">Semver</a> but is not restricted to those
 * patterns. Practically any string can be a version. The most important
 * functionality of a version is the ability to compare two versions, and this
 * class provides methods for creating, parsing, and comparing versions.</p>
 *
 * @see Release
 */
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class Version implements Comparable<Version> {

	/**
	 * The value of a version that is unknown.
	 */
	private static final String UNKNOWN = "unknown";

	/**
	 * The snapshot qualifier.
	 */
	private static final String SNAPSHOT = "snapshot";

	/**
	 * The milestone qualifier.
	 */
	private static final String MILESTONE = "milestone";

	/**
	 * The alpha qualifier.
	 */
	private static final String ALPHA = "alpha";

	/**
	 * The beta qualifier.
	 */
	private static final String BETA = "beta";

	/**
	 * The patch qualifier.
	 */
	private static final String PATCH = "patch";

	/**
	 * The update qualifier.
	 */
	private static final String UPDATE = "update";

	/**
	 * The expansions of qualifier abbreviations used to generate the
	 * human-friendly form of a version.
	 */
	private static final Map<String, String> expansions = new ConcurrentHashMap<>();

	static {
		expansions.put( "a", "Alpha" );
		expansions.put( ALPHA, "Alpha" );
		expansions.put( "b", "Beta" );
		expansions.put( BETA, "Beta" );
		expansions.put( "m", "Milestone" );
		expansions.put( MILESTONE, "Milestone" );
		expansions.put( "p", "Patch" );
		expansions.put( PATCH, "Patch" );
		expansions.put( "u", "Update" );
		expansions.put( UPDATE, "Update" );
		expansions.put( "cr", "Release Candidate" );
		expansions.put( "rc", "Release Candidate" );
		expansions.put( "ga", "" );
		expansions.put( "sp", "Service Pack" );
		expansions.put( SNAPSHOT, "SNAPSHOT" );
		expansions.put( UNKNOWN, "UNKNOWN" );
	}

	/**
	 * The human-friendly form of the version.
	 */
	private String human;

	/**
	 * The original string of the version.
	 */
	private String version;

	/**
	 * The canonical form of the version, used for equality and hashing.
	 */
	private String canonical;

	/**
	 * The parsed items of the version, in order of appearance.
	 */
	private ListItem items;

	/**
	 * The parts of the version string, used to generate the human-friendly
	 * form of the version.
	 */
	private List<Part> parts;

	/**
	 * Create a version with an unknown value.
	 */
	public Version() {
		this( UNKNOWN );
	}

	/**
	 * Create a version from a String.
	 *
	 * @param version The version string, or null for an unknown version
	 */
	public Version( String version ) {
		parse( version == null ? UNKNOWN : version );
	}

	/**
	 * Compare two versions.
	 *
	 * @param version1 The first version
	 * @param version2 The second version
	 * @return -1, 0 or 1
	 * @see #compareTo(Version)
	 */
	public static int compare( String version1, String version2 ) {
		return new Version( version1 ).compareTo( new Version( version2 ) );
	}

	/**
	 * Compare two versions.
	 *
	 * @param version1 The first version
	 * @param version2 The second version
	 * @return -1, 0 or 1
	 * @see #compareTo(Version)
	 */
	public static int compare( Version version1, Version version2 ) {
		return version1.compareTo( version2 );
	}

	/**
	 * Is this version a SNAPSHOT version? The version is a SNAPSHOT version if
	 * it has the SNAPSHOT qualifier.
	 *
	 * @return True if this version is a SNAPSHOT, false otherwise
	 */
	public boolean isSnapshot() {
		return hasQualifier( SNAPSHOT );
	}

	/**
	 * Does this version have a specific qualifier?
	 *
	 * @param qualifier The qualifier in question
	 * @return True if this version has the qualifier, false otherwise
	 */
	public boolean hasQualifier( String qualifier ) {
		return checkForString( items, qualifier.toLowerCase() );
	}

	/**
	 * Format the version as a human-friendly string.
	 *
	 * @return The version in human-friendly format
	 */
	public String toHumanString() {
		return this.human;
	}

	/**
	 * Compare this version to the specified version. The versions are compared
	 * one level at a time until the two parts at a level mismatch or the versions
	 * are found to be equal.
	 * <p>
	 * This method returns -1 if this version is less than the specified
	 * version, 1 if this version is greater than the specified version, or 0
	 * if the versions are equal.
	 * </p>
	 *
	 * @param version The version to compare with
	 * @return -1, 0 or 1
	 */
	@Override
	public int compareTo( Version version ) {
		int result = items.compareTo( version.items );
		if( result == 0 ) return 0;
		return result < 0 ? -1 : 1;
	}

	/**
	 * Return the string representation of this version.
	 *
	 * @return The string of this version
	 */
	@Override
	public String toString() {
		return version;
	}

	/**
	 * Two versions are equal if their canonical forms are equal.
	 *
	 * @param object The object to compare with
	 * @return {@code true} if the specified object is a version with the same
	 *     canonical form
	 */
	@Override
	public boolean equals( Object object ) {
		return (object instanceof Version) && canonical.equals( ((Version)object).canonical );
	}

	/**
	 * The hash code is derived from the canonical form of the version.
	 *
	 * @return The hash code of the version
	 */
	@Override
	public int hashCode() {
		return canonical.hashCode();
	}

	private void parse( String version ) {
		items = new ListItem();
		parts = new ArrayList<>();

		ListItem list = items;
		Stack<Item> stack = new Stack<>();
		String string = version.toLowerCase( Locale.ENGLISH );

		int startIndex = 0;
		boolean isDigit = false;
		stack.push( list );

		for( int index = 0; index < string.length(); index++ ) {
			char c = string.charAt( index );

			if( c == '.' ) {
				startIndex = getStartIndex( version, string, startIndex, index, isDigit, list );
				parts.add( new DividePart( "." ) );
			} else if( c == '-' ) {
				startIndex = getStartIndex( version, string, startIndex, index, isDigit, list );

				if( isDigit ) {
					list.normalize();

					if( (index + 1 < string.length()) && Character.isDigit( string.charAt( index + 1 ) ) ) {
						list.add( list = new ListItem() );
						stack.push( list );
					}
				}
				parts.add( new DividePart( "-" ) );
			} else if( Character.isDigit( c ) ) {
				if( !isDigit && index > startIndex ) {
					list.add( new StringItem( string.substring( startIndex, index ), true ) );
					parts.add( new StringPart( version.substring( startIndex, index ) ) );
					startIndex = index;
				}

				isDigit = true;
			} else {
				if( isDigit && index > startIndex ) {
					list.add( parse( true, string.substring( startIndex, index ) ) );
					parts.add( parsePart( true, version.substring( startIndex, index ) ) );
					startIndex = index;
				}

				isDigit = false;
			}
		}

		if( string.length() > startIndex ) {
			list.add( parse( isDigit, string.substring( startIndex ) ) );
			parts.add( parsePart( isDigit, version.substring( startIndex ) ) );
		}

		while( !stack.isEmpty() ) {
			list = (ListItem)stack.pop();
			list.normalize();
		}

		this.version = version;
		this.canonical = items.toString();
		this.human = generateHumanString();
	}

	private int getStartIndex( String version, String string, int startIndex, int currentIndex, boolean isDigit, ListItem list ) {
		if( currentIndex == startIndex ) {
			list.add( IntegerItem.ZERO );
			parts.add( new NumberPart( "0" ) );
		} else {
			list.add( parse( isDigit, string.substring( startIndex, currentIndex ) ) );
			parts.add( parsePart( isDigit, version.substring( startIndex, currentIndex ) ) );
		}
		return currentIndex + 1;
	}

	private Item parse( boolean digit, String buffer ) {
		return digit ? new IntegerItem( buffer ) : new StringItem( buffer, false );
	}

	private Part parsePart( boolean digit, String buffer ) {
		return digit ? new NumberPart( buffer ) : new StringPart( buffer );
	}

	private boolean checkForString( ListItem list, String string ) {
		boolean result = false;

		for( Item item : list ) {
			if( item instanceof ListItem ) {
				if( checkForString( (ListItem)item, string ) ) result = true;
			} else if( item instanceof StringItem ) {
				if( string.equals( expand( item.toString() ).toLowerCase() ) ) result = true;
			}
		}

		return result;
	}

	private String expand( String text ) {
		String result = expansions.get( text );
		return result == null ? text : result;
	}

	private String generateHumanString() {
		StringBuilder builder = new StringBuilder();

		Part prefix = null;
		Part previous = null;
		int count = parts.size();
		for( int index = 0; index < count; index++ ) {
			Part part = parts.get( index );
			if( index > 0 ) prefix = parts.get( index - 1 );
			if( part instanceof NumberPart ) {
				if( prefix instanceof DividePart && previous instanceof NumberPart ) builder.append( prefix );
				if( previous instanceof StringPart ) builder.append( " " );
				builder.append( part );
				previous = part;
			} else if( part instanceof StringPart ) {
				if( prefix != null ) builder.append( " " );
				builder.append( expand( part.toString() ) );
				previous = part;
			}

			prefix = part;
		}

		return builder.toString();
	}

	/**
	 * A parsed item in the version item list.
	 *
	 * <p>Items are the components of a version in order of appearance. They
	 * can be numeric, strings, or nested lists of items.</p>
	 */
	private interface Item {

		/**
		 * The type of an {@link IntegerItem}.
		 */
		int INTEGER_ITEM = 0;

		/**
		 * The type of a {@link StringItem}.
		 */
		int STRING_ITEM = 1;

		/**
		 * The type of a {@link ListItem}.
		 */
		int LIST_ITEM = 2;

		/**
		 * Compare this item to the specified item.
		 *
		 * @param item The item to compare with, or null
		 * @return A negative integer, zero, or a positive integer as this
		 *     item is less than, equal to, or greater than the specified item
		 */
		int compareTo( Item item );

		/**
		 * Get the type of this item.
		 *
		 * @return The type of this item
		 */
		int getType();

		/**
		 * Determines whether this item is considered null, meaning it has no
		 * significance when comparing versions.
		 *
		 * @return {@code true} if this item is considered null
		 */
		boolean isNull();

	}

	/**
	 * Represents a numeric item in the version item list.
	 */
	private static class IntegerItem implements Item {

		private static final BigInteger BigInteger_ZERO = new BigInteger( "0" );

		private static final IntegerItem ZERO = new IntegerItem();

		private final BigInteger value;

		private IntegerItem() {
			this.value = BigInteger_ZERO;
		}

		private IntegerItem( String string ) {
			this.value = new BigInteger( string );
		}

		@Override
		public int getType() {
			return INTEGER_ITEM;
		}

		@Override
		public boolean isNull() {
			return BigInteger_ZERO.equals( value );
		}

		@Override
		public int compareTo( Item item ) {
			if( item == null ) {
				return BigInteger_ZERO.equals( value ) ? 0 : 1;
			}

			return switch( item.getType() ) {
				case INTEGER_ITEM -> value.compareTo( ((IntegerItem)item).value );
				case STRING_ITEM, LIST_ITEM -> 1;
				default -> throw new RuntimeException( "Invalid item: " + item.getClass() );
			};
		}

		@Override
		public String toString() {
			return value.toString();
		}
	}

	/**
	 * Represents a string in the version item list, usually a qualifier.
	 */
	private static class StringItem implements Item {

		private static final List<String> QUALIFIERS = Arrays.asList( UNKNOWN, ALPHA, BETA, MILESTONE, "rc", SNAPSHOT, "", "sp" );

		private static final Map<String, String> ALIASES = new HashMap<>();

		/**
		 * A comparable value for the empty-string qualifier. This one is used to
		 * determine if a given qualifier makes the version older than one without a
		 * qualifier, or more recent.
		 */
		private static final String RELEASE_VERSION_INDEX = String.valueOf( QUALIFIERS.indexOf( "" ) );

		static {
			ALIASES.put( "ga", "" );
			ALIASES.put( "final", "" );
			ALIASES.put( "cr", "rc" );
			ALIASES.put( "patch", "sp" );
			ALIASES.put( "update", "sp" );
		}

		private String value;

		private StringItem( String value, boolean followedByDigit ) {
			if( followedByDigit && value.length() == 1 ) {
				// a1 = alpha-1, b1 = beta-1, m1 = milestone-1, p1 = patch-1, u1 = update-1
				switch( value.charAt( 0 ) ) {
					case 'a': {
						value = ALPHA;
						break;
					}
					case 'b': {
						value = BETA;
						break;
					}
					case 'm': {
						value = MILESTONE;
						break;
					}
					case 'p': {
						value = PATCH;
						break;
					}
					case 'u': {
						value = UPDATE;
						break;
					}
				}
			}

			this.value = ALIASES.get( value );
			if( this.value == null ) this.value = value;
		}

		/**
		 * Returns a comparable value for a qualifier. This method takes into
		 * account both the ordering of known qualifiers and lexical ordering
		 * for unknown qualifiers. Just returning an Integer with the index here is
		 * faster but requires a lot of if/then/else to check for -1 or
		 * QUALIFIERS.size and then resort to lexical ordering. Most comparisons are
		 * decided by the first character, so this is still fast. If more characters
		 * are needed, then it requires a lexical sort anyway.
		 *
		 * @param qualifier The qualifier for which to find an equivalent
		 * @return An equivalent value that can be used with lexical comparison
		 */
		private static String comparableQualifier( String qualifier ) {
			int index = QUALIFIERS.indexOf( qualifier );
			return index == -1 ? QUALIFIERS.size() + "-" + qualifier : String.valueOf( index );
		}

		@Override
		public int getType() {
			return STRING_ITEM;
		}

		@Override
		public boolean isNull() {
			return (comparableQualifier( value ).compareTo( RELEASE_VERSION_INDEX ) == 0);
		}

		@Override
		public int compareTo( Item item ) {
			if( item == null ) {
				return comparableQualifier( value ).compareTo( RELEASE_VERSION_INDEX );
			}
			return switch( item.getType() ) {
				case INTEGER_ITEM, LIST_ITEM -> -1;
				case STRING_ITEM -> comparableQualifier( value ).compareTo( comparableQualifier( ((StringItem)item).value ) );
				default -> throw new RuntimeException( "Invalid item: " + item.getClass() );
			};
		}

		@Override
		public String toString() {
			return value;
		}
	}

	/**
	 * A list of {@link Item} items in the version item list.
	 *
	 * <p>A list item groups the items that appear between two delimiters in
	 * the version string. List items are compared to other list items by
	 * comparing their items in order.</p>
	 */
	private static class ListItem extends ArrayList<Item> implements Item {

		@Serial
		private static final long serialVersionUID = -4773402270598497998L;

		@Override
		public int getType() {
			return LIST_ITEM;
		}

		@Override
		public boolean isNull() {
			return (size() == 0);
		}

		void normalize() {
			for( ListIterator<Item> iterator = listIterator( size() ); iterator.hasPrevious(); ) {
				Item item = iterator.previous();
				if( item.isNull() ) {
					iterator.remove();
				} else {
					break;
				}
			}
		}

		@Override
		public int compareTo( Item item ) {
			if( item == null ) {
				if( size() == 0 ) return 0;
				Item first = get( 0 );
				return first.compareTo( null );
			}
			switch( item.getType() ) {
				case INTEGER_ITEM: {
					return -1;
				}
				case STRING_ITEM: {
					return 1;
				}
				case LIST_ITEM: {
					Iterator<Item> leftIterator = iterator();
					Iterator<Item> rightIterator = ((ListItem)item).iterator();

					while( leftIterator.hasNext() || rightIterator.hasNext() ) {
						Item left = leftIterator.hasNext() ? leftIterator.next() : null;
						Item right = rightIterator.hasNext() ? rightIterator.next() : null;

						int result = 0;
						if( left != null ) result = left.compareTo( right );
						if( right != null ) result = -1 * right.compareTo( left );
						if( result != 0 ) return result;
					}

					return 0;
				}
				default: {
					throw new RuntimeException( "invalid item: " + item.getClass() );
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder( "(" );
			for( Iterator<Item> iterator = iterator(); iterator.hasNext(); ) {
				buffer.append( iterator.next() );
				if( iterator.hasNext() ) buffer.append( ',' );
			}
			buffer.append( ')' );
			return buffer.toString();
		}

	}

	/**
	 * A part of a version string, used to generate the human-friendly form
	 * of the version.
	 */
	private static abstract class Part {

		private final String value;

		Part( String value ) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

	}

	/**
	 * A part of a version string that is a delimiter, such as a dot or a
	 * dash.
	 */
	private static class DividePart extends Part {

		private DividePart( String value ) {
			super( value );
		}

	}

	/**
	 * A part of a version string that is a number.
	 */
	private static class NumberPart extends Part {

		private NumberPart( String value ) {
			super( value );
		}

	}

	/**
	 * A part of a version string that is a string, such as a qualifier.
	 */
	private static class StringPart extends Part {

		private StringPart( String value ) {
			super( value );
		}

	}

}
