package com.acromere.settings;

import com.acromere.util.PathUtil;
import com.acromere.util.TextUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory {@link Settings} implementation backed by concurrent maps.
 * <p>
 * Settings values are held in memory only and are not persisted. A settings
 * tree is rooted at a single {@code MapSettings} instance, and all nodes in
 * the tree share the root's node map. The values of a node are stored in a
 * {@link ConcurrentHashMap}, so nodes can be accessed from multiple
 * threads.
 * <p>
 * Nodes are created on demand with {@link #getNode(String)} and are never
 * removed except by {@link #delete()}.
 *
 * @see AbstractSettings
 * @see Settings
 */
public class MapSettings extends AbstractSettings {

	/**
	 * The root of the settings tree.
	 */
	private final MapSettings root;

	/**
	 * The absolute path of this settings node.
	 */
	private final String path;

	// The settings node values
	private final Map<String, String> values;

	// The map of settings. Should only be stored in the root node
	private Map<String, Settings> settings;

	// The settings defaults.
	private Map<String, Object> defaultValues;

	/**
	 * Creates a new in-memory settings tree with an empty root node.
	 */
	public MapSettings() {
		this( null, "/", new HashMap<>() );
	}

	private MapSettings( MapSettings root, String path, Map<String, String> values ) {
		if( root == null ) {
			this.settings = new ConcurrentHashMap<>();
			this.root = this;
		} else {
			this.root = root;
		}
		this.path = path;
		this.values = new ConcurrentHashMap<>();
		if( values != null ) this.values.putAll( values );
		this.root.settings.put( path, this );
	}

	@Override
	public String getName() {
		return path.equals( PathUtil.ROOT ) ? PathUtil.EMPTY : PathUtil.getName( path );
	}

	@Override
	public String getPath() {
		return path;
	}

	/**
	 * Check if the node at the specified path exists.
	 *
	 * @param path The node path to check
	 * @return True if the node exists, false otherwise
	 */
	@Override
	public boolean nodeExists( String path ) {
		return root.settings.get( getNodePath( this.path, path ) ) != null;
	}

	/**
	 * Get a settings object for the specified path, creating the node if
	 * it does not exist.
	 *
	 * @param path The requested path
	 * @return The settings object for the path
	 */
	@Override
	public Settings getNode( String path ) {
		return getNode( path, (Map<String, String>)null );
	}

	/**
	 * Get a settings object for the specified path according to
	 * {@link #getNode(String)}, initializing the node with the given
	 * values if it does not exist.
	 *
	 * @param path The requested path
	 * @param values The initial values of the settings
	 * @return The settings object for the path
	 */
	@Override
	public Settings getNode( String path, Map<String, String> values ) {
		String nodePath = getNodePath( this.path, path );

		// Get or create settings node
		Settings child = root.settings.get( nodePath );

		if( child == null ) child = new MapSettings( root, nodePath, values );

		return child;
	}

	/**
	 * Get the existing child node names of this settings node.
	 *
	 * @return The names of the existing child nodes
	 */
	@Override
	public List<String> getNodes() {
		List<String> children = new ArrayList<>();

		for( String childPath : root.settings.keySet() ) {
			if( !childPath.startsWith( path ) ) continue;
			String child = PathUtil.getChild( path, childPath );
			if( !TextUtil.isEmpty( child ) && !children.contains( child ) ) children.add( child );
		}

		return children;
	}

	/**
	 * Get the value keys for this settings node.
	 *
	 * @return The value keys for this settings node
	 */
	@Override
	public Set<String> getKeys() {
		return values.keySet();
	}

	/**
	 * Set the marshalled value for the given key. A {@code null} value
	 * removes the key.
	 *
	 * @param key The value key
	 * @param value The marshalled value
	 */
	@Override
	protected void setValue( String key, String value ) {
		if( value == null ) {
			values.remove( key );
		} else {
			values.put( key, value );
		}
	}

	/**
	 * Get the marshalled value for the given key.
	 *
	 * @param key The value key
	 * @return The marshalled value, or {@code null} if the key does not
	 *         exist
	 */
	@Override
	protected String getValue( String key ) {
		return values.get( key );
	}

	/**
	 * Flush the settings values. In-memory settings are always up to date,
	 * so this does nothing.
	 *
	 * @return This settings object
	 */
	@Override
	public Settings flush() {
		return this;
	}

	/**
	 * Delete this settings node, removing it from the settings tree and
	 * clearing its values.
	 *
	 * @return This settings object
	 */
	@Override
	public Settings delete() {
		root.settings.remove( getPath() );
		values.clear();
		return this;
	}

}
