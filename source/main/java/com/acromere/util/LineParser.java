package com.acromere.util;

import lombok.Getter;

/**
 * Incrementally parses the lines of a string, supporting {@code \n},
 * {@code \r}, and {@code \r\n} line terminators.
 *
 * <p>Use {@link #more() more()} to test for remaining lines and
 * {@link #next() next()} to advance to the next line. After calling
 * {@link #next() next()}, the current line and its terminator are available
 * via {@link #getLine() getLine()} and {@link #getTerminator()
 * getTerminator()}.</p>
 */
public class LineParser {

	private final String content;

	private final int length;

	private int next;

	@Getter
	private String line;

	private String term;

	/**
	 * Creates a parser for the given content.
	 *
	 * @param content The content to parse into lines, may be {@code null}
	 * (in which case no lines are available)
	 */
	public LineParser( String content ) {
		this.content = content;
		this.length = content == null ? -1 : content.length();
	}

	/**
	 * Checks whether there are more lines to read.
	 *
	 * @return {@code true} if {@link #next() next()} would return a line
	 */
	public boolean more() {
		return next <= length;
	}

	/**
	 * Advances to the next line.
	 *
	 * @return The next line (without its terminator), or {@code null} if
	 * there are no more lines
	 */
	public String next() {
		parseNextLine();
		return line;
	}

	/**
	 * Returns the terminator of the most recently read line.
	 *
	 * @return The line terminator ({@code "\n"}, {@code "\r"}, or
	 * {@code "\r\n"}), an empty string if the last line was not terminated,
	 * or {@code null} if no line has been read yet
	 */
	public String getTerminator() {
		return term;
	}

	/**
	 * Returns the portion of the content that has not yet been read.
	 *
	 * @return The remaining content, starting at the current parse position
	 */
	public String getRemaining() {
		return content.substring( next );
	}

	private void parseNextLine() {
		if( !more() ) {
			line = null;
			term = null;
			return;
		}

		int index = next;

		int c = index >= length ? -1 : content.charAt( index );
		while( c != '\n' && c != '\r' && c != -1 ) {
			index++;
			c = index >= length ? -1 : content.charAt( index );
		}

		int start = next;
		next = index;

		// Leave index at the end of the line and move next to the next line.
		if( c == '\r' ) {
			int c2 = index + 1 >= length ? -1 : content.charAt( index + 1 );
			next += c2 == '\n' ? 2 : 1;
		} else {
			next++;
		}

		line = content.substring( start, index );
		term = content.substring( index, Math.min( next, length ) );
	}

}
