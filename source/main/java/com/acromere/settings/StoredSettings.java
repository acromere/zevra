package com.acromere.settings;

import com.acromere.util.DelayedAction;
import com.acromere.util.FileUtil;
import com.acromere.util.PathUtil;
import com.acromere.util.TypeReference;
import lombok.CustomLog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A persistent {@link Settings} implementation backed by a folder of
 * files on the file system.
 * <p>
 * Each settings node is stored in a folder. Simple values are stored in a
 * {@value #SETTINGS_FILE_NAME} file, while beans, arrays, and collections
 * are stored in {@code .json} files named after the value key. Nodes are
 * created on demand, and missing parent nodes are created automatically.
 * <p>
 * Values are loaded from the file system when a node is created. Changes
 * are persisted asynchronously, at most once every
 * {@link #getMinFlushLimit()} milliseconds, and always within
 * {@link #getMaxFlushLimit()} milliseconds of the last change.
 * {@link #flush()} may be used to persist pending changes immediately.
 *
 * @see AbstractSettings
 * @see Settings
 */
@CustomLog
public class StoredSettings extends AbstractSettings {

	/**
	 * Data will be persisted at most this often, in milliseconds.
	 */
	private static final long MIN_PERSIST_LIMIT = 1000;

	/**
	 * Data will be persisted at least this often, in milliseconds.
	 */
	private static final long MAX_PERSIST_LIMIT = 5000;

	/**
	 * The file extension of the settings files.
	 */
	private static final String SETTINGS_EXTENSION = ".properties";

	/**
	 * The file name of the settings file of a node.
	 */
	private static final String SETTINGS_FILE_NAME = "settings" + SETTINGS_EXTENSION;

	private final DelayedAction action;

	private final StoredSettings root;

	private final String path;

	private final Path folder;

	private final Properties values;

	// Settings map store in root node
	private Map<String, StoredSettings> settings;

	/**
	 * Creates a new stored settings tree rooted at the given folder.
	 * <p>
	 * The folder name must not be the settings file name
	 * ({@value #SETTINGS_FILE_NAME}), as each settings node stores its
	 * simple values in a settings file in its folder.
	 *
	 * @param folder The root folder of the settings tree
	 */
	public StoredSettings( Path folder ) {
		this( folder, null );
	}

	private StoredSettings( Path folder, ExecutorService executor ) {
		this( null, "/", folder, null, executor );
	}

	private StoredSettings( StoredSettings root, String path, Path folder, Map<String, String> values, ExecutorService executor ) {
		if( folder.getFileName().toString().equals( SETTINGS_FILE_NAME ) ) throw new RuntimeException( "Folder ends with " + SETTINGS_FILE_NAME );
		if( root == null ) {
			this.settings = new ConcurrentHashMap<>();
			this.root = this;
		} else {
			this.root = root;
		}
		this.path = path;
		this.folder = folder;
		this.values = new Properties();
		if( values != null ) this.values.putAll( values );
		this.root.settings.put( path, this );

		this.action = new DelayedAction( executor, this::save );
		this.action.setMinTriggerLimit( MIN_PERSIST_LIMIT );
		this.action.setMaxTriggerLimit( MAX_PERSIST_LIMIT );

		load();
	}

	@Override
	public String getName() {
		return path.equals( PathUtil.ROOT ) ? PathUtil.EMPTY : folder.getFileName().toString();
	}

	@Override
	public String getPath() {
		return path;
	}

	/**
	 * Check if the node at the specified path exists. A node exists if it
	 * has been loaded or if its folder exists on the file system.
	 *
	 * @param path The node path to check
	 * @return True if the node exists, false otherwise
	 */
	@Override
	public boolean nodeExists( String path ) {
		String nodePath = getNodePath( this.path, path );
		if( root.settings.get( nodePath ) != null ) return true;

		// Check the file system
		Path folder = root.folder.resolve( nodePath.substring( 1 ) );
		return Files.exists( folder );
	}

	/**
	 * Get a settings object for the specified path, creating the node and
	 * any missing parent nodes if they do not exist.
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
		// Get or create settings node
		String nodePath = getNodePath( this.path, path );
		Settings node = root.settings.get( nodePath );
		Path folder = root.folder.resolve( nodePath.substring( 1 ) );
		if( node == null ) {
			log.atFiner().log( "node=%s", nodePath );
			node = new StoredSettings( root, nodePath, folder, values, action.getExecutor() );
		}
		// Create missing parents
		String parentPath = PathUtil.getParent( nodePath );
		folder = folder.getParent();
		while( parentPath != null && root.settings.get( parentPath ) == null ) {
			log.atFiner().log( "parent=%s", parentPath );
			new StoredSettings( root, parentPath, folder, null, action.getExecutor() );
			parentPath = PathUtil.getParent( parentPath );
			folder = folder.getParent();
		}

		return node;
	}

	/**
	 * Get the existing child node names of this settings node. Child
	 * nodes that are stored on the file system as well as child nodes
	 * that have been loaded are included.
	 *
	 * @return The names of the existing child nodes
	 */
	@Override
	public List<String> getNodes() {
		List<String> externalNames = new CopyOnWriteArrayList<>();
		if( Files.exists( folder ) ) {
			try( Stream<Path> list = Files.list( folder ) ) {
				list.parallel().filter( Files::isDirectory ).forEach( path -> externalNames.add( path.getFileName().toString() ) );
			} catch( IOException exception ) {
				log.atWarning().withCause( exception ).log( "Unable to list paths: %s", folder );
			}
		}

		String nodePath = this.path;
		int length = PathUtil.ROOT.equals( nodePath ) ? 1 : nodePath.length() + 1;
		List<String> internalNames = root.settings.keySet().stream().filter( k -> !k.equals( nodePath ) ).filter( k -> k.startsWith( nodePath ) ).map( n -> {
			// Parse out the child node name
			int index = n.indexOf( "/", length + 1 );
			if( index < 0 ) return n.substring( length );
			return n.substring( length, index );
		} ).distinct().toList();

		return Stream.concat( externalNames.stream(), internalNames.stream() ).distinct().toList();
	}

	/**
	 * Get the value keys for this settings node.
	 *
	 * @return The value keys for this settings node
	 */
	@Override
	public Set<String> getKeys() {
		Set<String> keys = new HashSet<>();
		for( Object key : values.keySet() ) {
			keys.add( key.toString() );
		}
		return keys;
	}

	/**
	 * Get the minimum time, in milliseconds, between persisted settings
	 * writes.
	 *
	 * @return The minimum time, in milliseconds, between persisted
	 *         settings writes
	 */
	public long getMinFlushLimit() {
		return action.getMinTriggerLimit();
	}

	/**
	 * Set the minimum time, in milliseconds, between persisted settings
	 * writes.
	 *
	 * @param limit The minimum time, in milliseconds, between persisted
	 *              settings writes
	 */
	public void setMinFlushLimit( long limit ) {
		action.setMinTriggerLimit( limit );
	}

	/**
	 * Get the maximum time, in milliseconds, before pending settings
	 * changes are persisted.
	 *
	 * @return The maximum time, in milliseconds, before pending settings
	 *         changes are persisted
	 */
	public long getMaxFlushLimit() {
		return action.getMaxTriggerLimit();
	}

	/**
	 * Set the maximum time, in milliseconds, before pending settings
	 * changes are persisted.
	 *
	 * @param limit The maximum time, in milliseconds, before pending
	 *              settings changes are persisted
	 */
	public void setMaxFlushLimit( long limit ) {
		action.setMaxTriggerLimit( limit );
	}

	/**
	 * Set the marshalled value for the given key. A {@code null} value
	 * removes the key and deletes the stored JSON file, if present. The
	 * change is scheduled for persistence.
	 *
	 * @param key The value key
	 * @param value The marshalled value
	 */
	@Override
	protected void setValue( String key, String value ) {
		if( value == null ) {
			values.remove( key );
			Path path = getJsonFilePath( key );
			if( Files.exists( path ) ) {
				try {
					Files.delete( path );
				} catch( IOException exception ) {
					log.atError( exception ).log( "Error removing value at=%s", path );
				}
			}
		} else {
			values.setProperty( key, value );
		}
		action.request();
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
		return values.getProperty( key );
	}

	/**
	 * Get a bean value, loading it from its JSON file if present,
	 * otherwise unmarshalling the stored value.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param <S> The type of the return value
	 * @return The bean value
	 */
	@Override
	protected <S> S getBean( String key, TypeReference<S> type ) {
		return load( key, type, () -> super.getBean( key, type ) );
	}

	/**
	 * Set a bean value, storing it in its JSON file.
	 *
	 * @param key The value key
	 * @param bean The bean value
	 */
	@Override
	protected <S> void setBean( String key, S bean ) {
		save( key, bean );
	}

	/**
	 * Get an array value, loading it from its JSON file if present,
	 * otherwise unmarshalling the stored value.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param <S> The type of the return value
	 * @return The array value
	 */
	@Override
	@SuppressWarnings( "unchecked" )
	protected <S> Object[] getArray( String key, TypeReference<S> type ) {
		return (Object[])load( key, type, () -> (S)super.getArray( key, type ) );
	}

	/**
	 * Set an array value, storing it in its JSON file.
	 *
	 * @param key The value key
	 * @param array The array value
	 */
	@Override
	protected void setArray( String key, Object[] array ) {
		save( key, array );
	}

	/**
	 * Get a collection value, loading it from its JSON file if present,
	 * otherwise unmarshalling the stored value.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param <S> The type of the return value
	 * @return The collection value
	 */
	@Override
	@SuppressWarnings( "unchecked" )
	protected <S> Collection<?> getCollection( String key, TypeReference<S> type ) {
		return (Collection<?>)load( key, type, () -> (S)super.getCollection( key, type ) );
	}

	/**
	 * Set a collection value, storing it in its JSON file.
	 *
	 * @param key The value key
	 * @param collection The collection value
	 */
	@Override
	protected void setCollection( String key, Collection<?> collection ) {
		save( key, collection );
	}

	/**
	 * Get a map value, loading it from its JSON file if present,
	 * otherwise unmarshalling the stored value.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param <S> The type of the return value
	 * @return The map value
	 */
	@Override
	@SuppressWarnings( "unchecked" )
	protected <S> Map<?, ?> getMap( String key, TypeReference<S> type ) {
		return (Map<?, ?>)load( key, type, () -> (S)super.getMap( key, type ) );
	}

	/**
	 * Set a map value, storing it in its JSON file.
	 *
	 * @param key The value key
	 * @param map The map value
	 */
	@Override
	protected void setMap( String key, Map<?, ?> map ) {
		save( key, map );
	}

	/**
	 * Load a value from its JSON file, or call the supplier if the file
	 * does not exist.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param supplier The supplier that provides the value from the
	 *                 stored value
	 * @param <S> The type of the return value
	 * @return The value, or {@code null} if the value could not be loaded
	 */
	protected <S> S load( String key, TypeReference<S> type, Supplier<S> supplier ) {
		Path path = getJsonFilePath( key );
		if( !Files.exists( path ) ) return supplier.get();
		try {
			String value = FileUtil.load( path );
			return unmarshallValue( value, type );
		} catch( IOException exception ) {
			log.atError( exception ).log( "Error loading value at=%s", path );
		}
		return null;
	}

	/**
	 * Save a value to its JSON file.
	 *
	 * @param key The value key
	 * @param value The value to save
	 */
	private void save( String key, Object value ) {
		Path path = getJsonFilePath( key );
		try {
			Files.createDirectories( path.getParent() );
			FileUtil.save( marshallValue( value ), path );
		} catch( IOException exception ) {
			log.atError( exception ).log( "Error saving value at=%s", path );
		}
	}

	/**
	 * Flush the settings values, persisting pending changes immediately.
	 *
	 * @return This settings object
	 */
	@Override
	public Settings flush() {
		action.trigger();
		return this;
	}

	/**
	 * Delete this settings node and all of its descendants, removing the
	 * corresponding files and folders.
	 *
	 * @return This settings object
	 */
	@Override
	public synchronized Settings delete() {
		action.cancel();
		setDeleted();
		getNodes().stream().map( this::getNode ).forEach( Settings::delete );

		try {
			// Delete the file
			Path file = getFile();
			if( !FileUtil.delete( file ) ) {
				log.atWarning().log( "Unable to delete file=%s", file );
			}

			// Delete the folder if empty
			if( getNodes().isEmpty() ) FileUtil.delete( folder );
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Unable to delete settings folder=%s", folder );
		} finally {
			root.settings.remove( getPath() );
		}

		return this;
	}

	/**
	 * Get the folder of this settings node.
	 *
	 * @return The folder of this settings node
	 */
	@Override
	public String toString() {
		return folder.toString();
	}

	/**
	 * Load the simple values of this settings node from its settings
	 * file, dispatching a {@link SettingsEvent} of type
	 * {@link SettingsEvent#LOADED} on success.
	 */
	private synchronized void load() {
		Path file = getFile();
		if( !Files.exists( file ) || Files.isDirectory( file ) ) return;
		try( FileInputStream input = new FileInputStream( file.toFile() ) ) {
			values.load( input );
			getEventHub().dispatch( new SettingsEvent( this, SettingsEvent.LOADED, getPath() ) );
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error loading settings file=%s", file );
		}
	}

	/**
	 * Save the simple values of this settings node to its settings file,
	 * dispatching a {@link SettingsEvent} of type
	 * {@link SettingsEvent#SAVED} on success.
	 */
	private synchronized void save() {
		if( isDeleted() ) return;

		Path file = getFile();
		try {
			Files.createDirectories( file.getParent() );
			try( FileOutputStream output = new FileOutputStream( file.toFile() ) ) {
				values.store( output, null );
				getEventHub().dispatch( new SettingsEvent( this, SettingsEvent.SAVED, getPath() ) );
			}
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error saving settings file=%s", file );
		}
	}

	/**
	 * Get the settings file of this settings node.
	 *
	 * @return The settings file of this settings node
	 */
	private Path getFile() {
		return folder.resolve( SETTINGS_FILE_NAME );
	}

	/**
	 * Get the JSON file of the value at the given key.
	 *
	 * @param key The value key
	 * @return The JSON file of the value
	 */
	private Path getJsonFilePath( String key ) {
		return folder.resolve( key + ".json" );
	}

}
