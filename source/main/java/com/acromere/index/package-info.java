/**
 * A simple, yet powerful, full-text search and index system.
 *
 * <p>Content is described with the {@link com.acromere.index.Document
 * Document} class, which holds the content to be indexed along with
 * metadata such as the URI, title, and tags. The content is split into
 * {@link com.acromere.index.Term Terms} by a {@link com.acromere.index.TermSource
 * TermSource}, which is either a {@link com.acromere.index.TextTermSource
 * TextTermSource} for plain text or an {@link com.acromere.index.HtmlTermSource
 * HtmlTermSource} for HTML. The terms are stored in an {@link
 * com.acromere.index.Index Index} as {@link com.acromere.index.Hit Hits} by
 * the {@link com.acromere.index.HitFinder HitFinder}.</p>
 *
 * <p>Searching is performed with the {@link com.acromere.index.Search Search}
 * interface, using a {@link com.acromere.index.IndexQuery IndexQuery} to
 * define the search terms. A {@link com.acromere.index.DirectSearch
 * DirectSearch} finds exact matches, while a {@link com.acromere.index.FuzzySearch
 * FuzzySearch} finds close matches using the Levenshtein distance algorithm.
 * Documents are submitted for indexing with the {@link
 * com.acromere.index.Indexer Indexer}, and searches are performed against
 * one or more indexes with the {@link com.acromere.index.Indexer#search(Search,
 * IndexQuery, Collection) Indexer.search} method.</p>
 *
 * <p>Example:</p>
 * <pre>
 * Indexer indexer = new Indexer( Path.of( "indexes" ) ).start();
 * Document document = new Document( uri, icon, title, content );
 * indexer.submit( document ).get().get();
 *
 * IndexQuery query = IndexQuery.builder().term( "search" ).build();
 * List&lt;Index&gt; indexes = List.of( indexer.getIndex( Index.DEFAULT ).get() );
 * Result&lt;List&lt;Hit&gt;&gt; results = Indexer.search( new FuzzySearch(), query, indexes );
 * </pre>
 */
package com.acromere.index;
