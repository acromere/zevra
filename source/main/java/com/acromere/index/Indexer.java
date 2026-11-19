package com.acromere.index;

import com.acromere.result.Result;
import com.acromere.skill.Controllable;
import lombok.CustomLog;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Indexes {@link Document Documents} and maintains a set of named {@link
 * Index Indexes}.
 *
 * <p>The indexer implements {@link Controllable} and must be {@link
 * #start() started} before documents can be submitted. Documents are
 * submitted with the {@link #submit(Document) submit} methods and indexed
 * asynchronously on a thread pool. The index for a document is selected by
 * name, or defaults to the {@link Index#DEFAULT} index. The index path is a
 * location where index files can be placed.</p>
 */
@CustomLog
public class Indexer implements Controllable<Indexer> {

	/**
	 * A path to place index files.
	 */
	private final Path indexPath;

	/**
	 * A map of index name to index.
	 */
	private final Map<String, Index> indexes;

	/**
	 * A thread pool to run indexing tasks.
	 */
	private ExecutorService executor;

	/**
	 * Create an indexer that places index files at the specified path.
	 *
	 * @param indexPath A path to place index files
	 */
	public Indexer( Path indexPath ) {
		this.indexPath = indexPath;
		this.indexes = new ConcurrentHashMap<>();
	}

	/**
	 * Search a collection of indexes with the specified search and query.
	 * The indexes are merged into a single index before the search is
	 * performed.
	 *
	 * @param search  The search to perform
	 * @param query   The search query
	 * @param indexes The indexes to search
	 * @return The list of hits found by the search
	 */
	public static Result<List<Hit>> search( Search search, IndexQuery query, Collection<Index> indexes ) {
		return search.search( Index.merge( indexes ), query );
	}

	@Override
	public boolean isRunning() {
		return executor != null && !executor.isShutdown();
	}

	@Override
	public Indexer start() {
		executor = Executors.newCachedThreadPool();
		// Start should load exising indexes
		return this;
	}

	@Override
	public Indexer stop() {
		if( executor != null ) executor.shutdown();
		return this;
	}

	/**
	 * Submit a document to the {@link Index#DEFAULT default index}.
	 *
	 * @param document The document to index
	 * @return A future for the result of the index operation, or a failure if
	 *         the indexer is not running
	 */
	public Result<Future<Result<Set<Hit>>>> submit( Document document ) {
		return submit( Index.DEFAULT, document );
	}

	/**
	 * Submit a document to the named index.
	 *
	 * @param index    The name of the index to index the document into
	 * @param document The document to index
	 * @return A future for the result of the index operation, or a failure if
	 *         the indexer is not running
	 */
	public Result<Future<Result<Set<Hit>>>> submit( String index, Document document ) {
		if( !isRunning() ) return Result.of( new IllegalStateException( "Indexer not running" ) );
		return Result.of( executor.submit( () -> doIndex( index, document ) ) );
	}

	/**
	 * Submit documents to the {@link Index#DEFAULT default index}.
	 *
	 * @param documents The documents to index
	 * @return A set of futures for the result of each index operation, or a
	 *         failure if the indexer is not running
	 */
	public Result<Set<Future<Result<Set<Hit>>>>> submit( Document... documents ) {
		return submit( Index.DEFAULT, documents );
	}

	/**
	 * Submit documents to the named index.
	 *
	 * @param index     The name of the index to index the documents into
	 * @param documents The documents to index
	 * @return A set of futures for the result of each index operation, or a
	 *         failure if the indexer is not running
	 */
	public Result<Set<Future<Result<Set<Hit>>>>> submit( String index, Document... documents ) {
		if( !isRunning() ) return Result.of( new IllegalStateException( "Indexer not running" ) );
		return Result.of( Arrays.stream( documents ).map( d -> executor.submit( () -> doIndex( index, d ) ) ).collect( Collectors.toSet() ) );
	}

	/**
	 * Get all the indexes.
	 *
	 * @return A set of all the indexes
	 */
	public Set<Index> allIndexes() {
		return Set.copyOf( indexes.values() );
	}

	/**
	 * Get an index by name.
	 *
	 * @param index The name of the index
	 * @return An optional containing the index, or empty if the index does
	 *         not exist
	 */
	public Optional<Index> getIndex( String index ) {
		return Optional.ofNullable( indexes.get( index ) );
	}

	/**
	 * Remove an index by name.
	 *
	 * @param index The name of the index to remove
	 */
	public void removeIndex( String index ) {
		indexes.remove( index );
	}

	/**
	 * Index a document into the named index. The term source is selected by
	 * the document {@link Document#mediaType() media type}.
	 *
	 * @param name     The name of the index to index the document into
	 * @param document The document to index
	 * @return The set of hits found in the document
	 */
	private Result<Set<Hit>> doIndex( String name, Document document ) {
		Index index = indexes.computeIfAbsent( name, k -> new StandardIndex() );

		TermSource parser = switch( document.mediaType() ) {
			case HTML -> new HtmlTermSource( document );
			default -> new TextTermSource( document );
		};

		return new HitFinder().find( document, parser ).ifSuccess( index::push ).ifFailure( e -> log.atWarn( e ).log( "Unable to parse document: %s", document ) );
	}

}
