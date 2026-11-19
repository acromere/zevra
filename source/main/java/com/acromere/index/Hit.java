package com.acromere.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * This class represents both index and search hits since both of these hit
 * types are very similar and have very similar uses. Index hits have all
 * the information regarding hits while indexing. Search hits can augment index
 * hits with more information regarding the search match.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors( chain = true )
public class Hit {

	/**
	 * The search priority for hits found in a document tag. Tag hits are
	 * given the lowest priority.
	 */
	public static final int TAG_PRIORITY = 2;

	/**
	 * The search priority for hits found in a document title. Title hits are
	 * given a medium priority.
	 */
	public static final int TITLE_PRIORITY = 1;

	/**
	 * The search priority for hits found in a document's content. Content
	 * hits are given the highest priority.
	 */
	public static final int CONTENT_PRIORITY = 0;

	/**
	 * The highest hit priority, which is the {@link #CONTENT_PRIORITY content
	 * priority}.
	 */
	private static final int HIGHEST_PRIORITY = CONTENT_PRIORITY;

	/**
	 * The lowest hit priority, which is the {@link #TAG_PRIORITY tag
	 * priority}.
	 */
	private static final int LOWEST_PRIORITY = TAG_PRIORITY;

	/**
	 * The document the hit was found in.
	 */
	private Document document;

	/**
	 * The context of the hit, such as the line of text the hit was found on.
	 */
	private String context;

	/**
	 * The lower case word the hit was found for.
	 */
	private String word;

	/**
	 * The length of the hit word.
	 */
	private int length;

	/**
	 * The coordinates of the hit in the document.
	 */
	private List<Integer> coordinates;

	/**
	 * The search points for the hit. The points indicate how well the hit
	 * matched the search terms. This is used for search hits.
	 */
	private int points;

	/**
	 * The priority of the hit.
	 */
	private int priority;

	@Override
	public String toString() {
		return "{" + "title=" + document.title() + " length=" + length + " term=" + word + " context=" + context + " coords=" + coordinates + "}";
	}

}
