package com.acromere.index;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Utility methods for splitting text into terms. A term is a sequence of
 * letters and digits.
 */
public class Terms {

	/**
	 * Split the text into a list of terms.
	 *
	 * @param text The text to split
	 * @return A list of the terms in the text
	 */
	public static List<String> split( String text ) {
		return split( text, text::substring );
	}

	/**
	 * Split the text into terms. For each term, the consumer is called with
	 * the start and end indexes of the term in the text.
	 *
	 * @param text     The text to split
	 * @param consumer A function applied to the start and end indexes of
	 *                 each term
	 * @param <T>      The type of term the consumer returns
	 * @return A list of the terms in the text
	 */
	public static <T> List<T> split( String text, BiFunction<Integer, Integer, T> consumer ) {
		List<T> terms = new ArrayList<>();

		int point;
		int index = 0;
		int start = -1;
		int length = text.length();
		int endIndex = length - 1;
		String hitText = text.toLowerCase();
		while( index < length ) {
			point = hitText.codePointAt( index );

			// Are we at the last character in the line?
			boolean lastChar = index >= endIndex;
			// Is the character a word character?
			boolean wordChar = Character.isLetterOrDigit( point );
			// Is this character the start of a word?
			boolean startOfWord = wordChar && start < 0;
			// At the start of a word just set the word start index
			if( startOfWord ) start = index;

			// Is this character the end of a word?
			boolean endOfWord = start >= 0 && (!wordChar || lastChar);
			//System.out.println( "start=" + start + " wordChar=" + wordChar + " lastChar=" + lastChar + " endOfWord=" + endOfWord );

			// At the end of a word store a hit and reset the word start index
			if( endOfWord ) {
				//System.out.println( "start=" + start + " end=" + ((wordChar && lastChar) ? length : index) );
				terms.add( consumer.apply( start, wordChar ? length : index ) );
				start = -1;
			}

			index++;
		}

		return terms;
	}

}
