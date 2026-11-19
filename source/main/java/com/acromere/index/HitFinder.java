package com.acromere.index;

import com.acromere.result.Result;
import lombok.CustomLog;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds {@link Hit Hits} in a {@link Document}.
 *
 * <p>Hit finding is done in three passes. First, the document tags are
 * indexed with the {@link Hit#TAG_PRIORITY tag priority}. Next, the document
 * title is indexed with the {@link Hit#TITLE_PRIORITY title priority}.
 * Finally, the document content is indexed with the {@link
 * Hit#CONTENT_PRIORITY content priority} using the specified term source.
 * Hits from all three passes are combined and returned.</p>
 */
@CustomLog
public class HitFinder {

	/**
	 * Find all hits in the document.
	 *
	 * @param document The document to find hits in
	 * @param source   The term source for the document content
	 * @return The set of hits found in the document
	 */
	public Result<Set<Hit>> find( Document document, TermSource source ) {
		Set<Hit> hits = new HashSet<>();

		// Add tags
		hits.addAll( findHits( document, document.tags(), Hit.TAG_PRIORITY ) );

		// Add name
		hits.addAll( findHits( document, document.title(), Hit.TITLE_PRIORITY ) );

		// Add content
		hits.addAll( findHits( document, source, Hit.CONTENT_PRIORITY ) );

		return Result.of( hits );

	}

	/**
	 * Find hits for a set of content strings, such as the document tags.
	 *
	 * @param document The document the content belongs to
	 * @param content  The content to find hits in
	 * @param priority The priority of the hits
	 * @return The set of hits found in the content
	 */
	@SuppressWarnings( "SameParameterValue" )
	private Set<Hit> findHits( Document document, Set<String> content, int priority ) {
		return content.stream().flatMap( t -> findHits( document, t, priority ).stream() ).collect( Collectors.toSet() );
	}

	/**
	 * Find hits for a content string, such as the document title.
	 *
	 * @param document The document the content belongs to
	 * @param content  The content to find hits in
	 * @param priority The priority of the hits
	 * @return The set of hits found in the content
	 */
	private Set<Hit> findHits( Document document, String content, int priority ) {
		return findHits( document, new TextTermSource( document, content ), priority );
	}

	/**
	 * Find hits in the document content using the specified term source.
	 *
	 * @param document The document the content belongs to
	 * @param source   The term source for the document content
	 * @param priority The priority of the hits
	 * @return The set of hits found in the content
	 */
	public Set<Hit> findHits( Document document, TermSource source, int priority ) {
		Set<Hit> hits = new HashSet<>();

		try {
			hits.addAll( source
				.index()
				.map( t -> new Hit().setDocument( document ).setContext( t.context() ).setWord( t.word() ).setLength( t.length() ).setCoordinates( t.coordinates() ).setPriority( priority ) )
				.toList() );
		} catch( IOException exception ) {
			log.atWarn( exception );
		}

		return hits;
	}

}
