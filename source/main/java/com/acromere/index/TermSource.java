package com.acromere.index;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * A source of {@link Term Terms} for indexing a {@link Document}.
 *
 * <p>Implementations split the document content into terms. The
 * {@link #index()} method returns a stream of the terms found in the
 * document. Implementations in this package are a {@link TextTermSource} for
 * plain text and an {@link HtmlTermSource} for HTML.</p>
 */
public interface TermSource {

	/**
	 * Index the document and return a stream of the terms found.
	 *
	 * @return A stream of the terms found in the document
	 * @throws IOException If the document content cannot be read
	 */
	Stream<Term> index() throws IOException;

}
