package com.acromere.index;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A search query for an {@link Index}.
 *
 * <p>A query is a list of terms to search for. The query is created with the
 * builder:</p>
 * <pre>
 * IndexQuery query = IndexQuery.builder().term( "search" ).term( "term" ).build();
 * </pre>
 */
@Data
@Builder
@Accessors( fluent = true )
public class IndexQuery {

	/**
	 * The search terms.
	 */
	@Singular
	private List<String> terms;

}
