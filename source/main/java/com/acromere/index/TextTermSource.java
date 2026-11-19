package com.acromere.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A term source that indexes plain text content.
 *
 * <p>The text is read line by line and each line is split into terms with
 * {@link Terms#split(String, java.util.function.BiFunction)}. The term
 * context is the line the term was found on and the term coordinates are the
 * line index and the character index of the term in the line.</p>
 */
public class TextTermSource implements TermSource {

	private final Document document;

	private final String text;

	/**
	 * Create a text term source for the content of the document.
	 *
	 * @param document The document to index
	 */
	public TextTermSource( Document document ) {
		this( document, null );
	}

	/**
	 * Create a text term source for the specified text.
	 *
	 * @param document The document the text belongs to
	 * @param text     The text to index
	 */
	public TextTermSource( Document document, String text ) {
		this.document = document;
		this.text = text;
	}

	@Override
	public Stream<Term> index() throws IOException {
		return getTerms( text == null ? document.reader() : new StringReader( text ) ).stream();
	}

	private List<Term> getTerms( Reader source ) throws IOException {
		List<Term> terms = new ArrayList<>();

		try( BufferedReader reader = new BufferedReader( source ) ) {
			String text;
			int lineIndex = 0;
			while( (text = reader.readLine()) != null ) {
				final String line = text.trim();
				final int finalLineIndex = lineIndex;

				terms.addAll( Terms.split(
					line, ( start, end ) -> {
						int length = end - start;
						String word = line.substring( start, end ).toLowerCase();
						return new Term( line, word, length, List.of( finalLineIndex, start ) );
					}
				) );

				lineIndex++;
			}
		}

		return terms;
	}

}
