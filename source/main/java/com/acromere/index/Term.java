package com.acromere.index;

import java.util.List;

/**
 * A term found in a {@link Document} while indexing.
 *
 * @param context     The context of the term, such as the line of text the
 *                    term was found on. The context is null when it is not
 *                    defined.
 * @param word        The lower case word the term represents
 * @param length      The length of the term word
 * @param coordinates The coordinates of the term in the document
 */
public record Term( String context, String word, int length, List<Integer> coordinates ) {
}
