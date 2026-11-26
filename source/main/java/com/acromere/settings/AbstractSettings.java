package com.acromere.settings;

import com.acromere.event.Event;
import com.acromere.event.EventHandler;
import com.acromere.event.EventHub;
import com.acromere.event.EventType;
import com.acromere.util.PathUtil;
import com.acromere.util.TextUtil;
import com.acromere.util.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.CustomLog;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Base implementation of the {@link Settings} interface.
 * <p>
 * This class provides the shared behavior for all settings implementations,
 * including value marshalling and unmarshalling, default value resolution,
 * and settings change event handling. Simple values (boolean, character,
 * byte, short, integer, long, float, double, string, URI, and file) are
 * converted to and from their string representation. All other values are
 * marshalled and unmarshalled using JSON.
 * <p>
 * Subclasses must implement {@link #getValue(String)} and
 * {@link #setValue(String, String)} to provide storage of the marshalled
 * values, and may override the other get and set methods to optimize for
 * their storage strategy.
 *
 * @see Settings
 * @see MapSettings
 * @see StoredSettings
 */
@CustomLog
public abstract class AbstractSettings implements Settings {

	private static final Map<Class<?>, OutboundConverter> outboundConverters;

	private static final Map<Class<?>, InboundConverter> inboundConverters;

	static {
		outboundConverters = new HashMap<>();
		outboundConverters.put( Boolean.class, String::valueOf );
		outboundConverters.put( Character.class, String::valueOf );
		outboundConverters.put( Byte.class, String::valueOf );
		outboundConverters.put( Short.class, String::valueOf );
		outboundConverters.put( Integer.class, String::valueOf );
		outboundConverters.put( Long.class, String::valueOf );
		outboundConverters.put( Float.class, String::valueOf );
		outboundConverters.put( Double.class, String::valueOf );
		outboundConverters.put( String.class, ( value ) -> (String)value );
		outboundConverters.put( URI.class, Object::toString );
		outboundConverters.put( File.class, ( value ) -> ((File)value).toURI().toString() );

		inboundConverters = new HashMap<>();
		inboundConverters.put( Boolean.class, ( value ) -> value == null ? null : Boolean.parseBoolean( value ) );
		inboundConverters.put( Character.class, ( value ) -> value == null ? null : value.charAt( 0 ) );
		inboundConverters.put( Byte.class, ( value ) -> value == null ? null : Byte.parseByte( value ) );
		inboundConverters.put( Short.class, ( value ) -> value == null ? null : Short.parseShort( value ) );
		inboundConverters.put( Integer.class, ( value ) -> value == null ? null : Integer.parseInt( value ) );
		inboundConverters.put( Long.class, ( value ) -> value == null ? null : Long.parseLong( value ) );
		inboundConverters.put( Float.class, ( value ) -> value == null ? null : Float.parseFloat( value ) );
		inboundConverters.put( Double.class, ( value ) -> value == null ? null : Double.parseDouble( value ) );
		inboundConverters.put( String.class, ( value ) -> value );
		inboundConverters.put( URI.class, ( value ) -> value == null ? null : URI.create( value ) );
		inboundConverters.put( File.class, ( value ) -> value == null ? null : new File( URI.create( value ) ) );
	}

	// FIXME The settings event hubs are not linked to their parents
	//  ...but should they?
	/**
	 * The event hub of this settings node.
	 */
	@Getter
	private final EventHub eventHub;

	/**
	 * The value change handlers, keyed by the value key.
	 */
	private final Map<String, Set<EventHandler<SettingsEvent>>> valueChangeHandlers;

	private Map<?, ?> defaultValues;

	private boolean deleted;

	/**
	 * Creates a new abstract settings instance.
	 */
	protected AbstractSettings() {
		this.eventHub = new EventHub();
		this.valueChangeHandlers = new ConcurrentHashMap<>();
		this.eventHub.register( SettingsEvent.CHANGED, this::dispatch );
	}

	/**
	 * Check if a specific value exists. A value exists if
	 * {@link #get(String)} returns a non-null value.
	 *
	 * @param path The value key
	 * @return True if the value exists, false otherwise
	 */
	@Override
	public boolean exists( String path ) {
		return getValue( path ) != null;
	}

	/**
	 * Get a value from the settings node, converting it to the given type,
	 * or returning the default value if the setting value is null.
	 * <p>
	 * Simple values are converted from their string representation, using
	 * the default value as a fallback if the setting value is null.
	 * Collections, maps, and arrays are unmarshalled from their stored
	 * representation. Any other value is unmarshalled as a bean.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param defaultValue The default value if the setting value is null
	 * @param <T> The type of the return value
	 * @return The setting value or the default value
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T get( String key, TypeReference<T> type, T defaultValue ) {
		Object value;
		Class<T> typeClass = type.getTypeClass();
		InboundConverter converter = inboundConverters.get( typeClass );

		// Unmarshall the value
		if( converter != null ) {
			value = converter.convert( getValue( key ) );
			if( value == null ) value = converter.convert( getDefault( key ) );
		} else if( Collection.class.isAssignableFrom( typeClass ) ) {
			value = getCollection( key, type );
		} else if( Map.class.isAssignableFrom( typeClass ) ) {
			value = getMap( key, type );
		} else if( typeClass.isArray() ) {
			value = getArray( key, type );
		} else {
			value = getBean( key, type );
		}

		// If the value is still null use the default value
		if( value == null ) value = defaultValue;

		return (T)value;
	}

	/**
	 * Get the default value for the given key as a string.
	 *
	 * @param key The value key
	 * @return The default value, or {@code null} if the key has no
	 *         default value
	 */
	private String getDefault( String key ) {
		Map<?, ?> defaultValues = getDefaultValues();
		if( defaultValues == null ) return null;

		Object objectValue = defaultValues.get( key );
		return objectValue == null ? null : objectValue.toString();
	}

	/**
	 * Set a value in the settings node.
	 * <p>
	 * Simple values are converted to their string representation.
	 * Collections, maps, and arrays are marshalled to JSON. Any other value
	 * is treated as a bean and marshalled to JSON. A {@code null} value
	 * removes the setting.
	 * <p>
	 * A {@link SettingsEvent} of type {@link SettingsEvent#CHANGED} is
	 * dispatched to the event hub if the marshalled value changed.
	 *
	 * @param key The value key
	 * @param value The value
	 * @return This settings object
	 */
	@Override
	public Settings set( String key, Object value ) {
		// Get the marshalled old value
		String oldValue = get( key );

		if( value == null ) {
			setValue( key, null );
		} else if( outboundConverters.containsKey( value.getClass() ) ) {
			setValue( key, outboundConverters.get( value.getClass() ).convert( value ) );
		} else if( value instanceof Collection ) {
			setCollection( key, (Collection<?>)value );
		} else if( value instanceof Map ) {
			setMap( key, (Map<?, ?>)value );
		} else if( value.getClass().isArray() ) {
			setArray( key, (Object[])value );
		} else {
			setBean( key, value );
		}

		// Get the marshalled new value
		String newValue = get( key );

		// Settings change event should only be fired if the values are different
		if( !Objects.equals( oldValue, newValue ) ) getEventHub().dispatch( new SettingsEvent( this, SettingsEvent.CHANGED, getPath(), key, value ) );

		return this;
	}

	/**
	 * Copy the values from the specified settings object to this settings
	 * object.
	 * <p>
	 * The values of this node and the values of all descendant nodes are
	 * copied.
	 *
	 * @param settings The settings from which to get values
	 * @return This settings object
	 */
	@Override
	public Settings copyFrom( Settings settings ) {
		// Shallow
		for( String key : settings.getKeys() ) {
			set( key, settings.get( key ) );
		}
		// Deep
		for( String child : settings.getNodes() ) {
			getNode( child ).copyFrom( settings.getNode( child ) );
		}
		return this;
	}

	/**
	 * Remove a value from the settings node. This is the same as setting
	 * the value to null.
	 *
	 * @param key The value key
	 * @return This settings object
	 */
	@Override
	public Settings remove( String key ) {
		set( key, null );
		return this;
	}

	/**
	 * Override this method to optimize retrieving simple values.
	 *
	 * @param key The value key
	 * @return The unmarshalled simple value
	 */
	protected abstract String getValue( String key );

	/**
	 * Override this method to optimize retrieving bean values.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param <S> The type of the return value
	 * @return The unmarshalled bean value
	 */
	protected <S> S getBean( String key, TypeReference<S> type ) {
		String value = getValue( key );
		if( value != null && type.getTypeClass().isEnum() ) {
			value = value.toUpperCase();
			if( !value.startsWith( "\"" ) ) value = "\"" + value;
			if( !value.endsWith( "\"" ) ) value = value + "\"";
		}
		return unmarshallValue( value, type );
	}

	/**
	 * Override this method to optimize retrieving array values.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param <S> The type of the return value
	 * @return The unmarshalled array value
	 */
	protected <S> Object[] getArray( String key, TypeReference<S> type ) {
		return (Object[])unmarshallValue( getValue( key ), type );
	}

	/**
	 * Override this method to optimize retrieving collection values.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param <S> The type of the return value
	 * @return The unmarshalled collection value
	 */
	protected <S> Collection<?> getCollection( String key, TypeReference<S> type ) {
		return (Collection<?>)unmarshallValue( getValue( key ), type );
	}

	/**
	 * Override this method to optimize retrieving map values.
	 *
	 * @param key The value key
	 * @param type The type of the return value
	 * @param <S> The type of the return value
	 * @return The unmarshalled map value
	 */
	protected <S> Map<?, ?> getMap( String key, TypeReference<S> type ) {
		return (Map<?, ?>)unmarshallValue( getValue( key ), type );
	}

	/**
	 * Override this method to optimize storing simple values.
	 *
	 * @param key The settings value key
	 * @param value The settings value
	 */
	protected abstract void setValue( String key, String value );

	/**
	 * Override this method to optimize storing bean values.
	 *
	 * @param key The settings value key
	 * @param value The settings value
	 * @param <S> The type of the settings value
	 */
	protected <S> void setBean( String key, S value ) {
		setValue( key, marshallValue( value ) );
	}

	/**
	 * Override this method to optimize storing array values.
	 *
	 * @param key The settings value key
	 * @param array The settings value
	 */
	protected void setArray( String key, Object[] array ) {
		setValue( key, marshallValue( array ) );
	}

	/**
	 * Override this method to optimize storing collection values.
	 *
	 * @param key The settings value key
	 * @param collection The settings value
	 */
	protected void setCollection( String key, Collection<?> collection ) {
		setValue( key, marshallValue( collection ) );
	}

	/**
	 * Override this method to optimize storing map values.
	 *
	 * @param key The settings value key
	 * @param map The settings value
	 */
	protected void setMap( String key, Map<?, ?> map ) {
		setValue( key, marshallValue( map ) );
	}

	/**
	 * Marshal the value to a string using JSON.
	 *
	 * @param value The value to marshal
	 * @return The marshalled value, or {@code null} if the value could not
	 *         be marshalled
	 */
	protected String marshallValue( Object value ) {
		try {
			return new ObjectMapper().writeValueAsString( value );
		} catch( JsonProcessingException exception ) {
			log.atWarning().withCause( exception ).log( "Error marshalling value" );
			return null;
		}
	}

	/**
	 * Unmarshall the string to a value of the given type using JSON.
	 *
	 * @param value The marshalled value
	 * @param type The type of the return value
	 * @param <T> The type of the return value
	 * @return The unmarshalled value, or {@code null} if the value is
	 *         {@code null} or could not be unmarshalled
	 */
	protected <T> T unmarshallValue( String value, TypeReference<T> type ) {
		if( value == null ) return null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readerFor( mapper.constructType( type.getType() ) ).readValue( value );
		} catch( IOException exception ) {
			log.atWarning().withCause( exception ).log( "Error unmarshalling value" );
			return null;
		}
	}

	/**
	 * Check if this settings node has been deleted.
	 *
	 * @return True if the settings node has been deleted, false otherwise
	 */
	protected boolean isDeleted() {
		return deleted;
	}

	/**
	 * Mark this settings node as deleted.
	 */
	protected void setDeleted() {
		this.deleted = true;
	}

	/**
	 * Get a settings object for the specified parent path and name. The
	 * parent path is resolved according to {@link #getNode(String)}.
	 *
	 * @param parent The parent path
	 * @param name The node name
	 * @return The settings object for the path
	 */
	@Override
	public Settings getNode( String parent, String name ) {
		return getNode( PathUtil.resolve( parent, name ) );
	}

	/**
	 * Get the default values for this settings node.
	 *
	 * @return The default values map, or {@code null} if no default values
	 *         have been set
	 */
	@Override
	public Map<?, ?> getDefaultValues() {
		return defaultValues;
	}

	/**
	 * Set the default values for this settings node.
	 *
	 * @param defaultValues The default values map
	 */
	@Override
	public void setDefaultValues( Map<?, ?> defaultValues ) {
		this.defaultValues = defaultValues == null ? null : Collections.unmodifiableMap( defaultValues );
	}

	/**
	 * Load the default values for this settings node from a properties
	 * file on the class path.
	 *
	 * @param source The class to load the default values resource from
	 * @param path The path of the default values resource
	 * @throws IOException If the default values could not be read
	 */
	@Override
	public void loadDefaultValues( Object source, String path ) throws IOException {
		Properties properties = new Properties();
		InputStream defaultSettingsInput = source.getClass().getResourceAsStream( path );
		if( defaultSettingsInput != null ) properties.load( new InputStreamReader( defaultSettingsInput, TextUtil.CHARSET ) );
		setDefaultValues( properties );
	}

	/**
	 * Register an event handler for the given event type.
	 *
	 * @param type The event type
	 * @param handler The event handler
	 * @param <T> The type of event handled
	 * @return The event hub
	 */
	@Override
	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return eventHub.register( type, handler );
	}

	/**
	 * Unregister an event handler for the given event type.
	 *
	 * @param type The event type
	 * @param handler The event handler
	 * @param <T> The type of event handled
	 * @return The event hub
	 */
	@Override
	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return eventHub.unregister( type, handler );
	}

	/**
	 * Register a handler for changes to the value at the given key.
	 *
	 * @param key The value key
	 * @param handler The event handler
	 */
	@Override
	public void register( String key, EventHandler<SettingsEvent> handler ) {
		valueChangeHandlers.computeIfAbsent( key, ( k ) -> new CopyOnWriteArraySet<>() ).add( handler );
	}

	/**
	 * Unregister a handler for changes to the value at the given key.
	 *
	 * @param key The value key
	 * @param handler The event handler
	 */
	@Override
	public void unregister( String key, EventHandler<? extends SettingsEvent> handler ) {
		valueChangeHandlers.computeIfAbsent( key, ( k ) -> new CopyOnWriteArraySet<>() ).remove( handler );
	}

	/**
	 * Get the event handlers registered on this settings node.
	 *
	 * @return The event handlers
	 */
	@Override
	public Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		return eventHub.getEventHandlers();
	}

	/**
	 * Resolve the node path. If the path is absolute, it is returned
	 * as-is. If the path is relative, it is resolved against the root
	 * path.
	 *
	 * @param root The root path
	 * @param path The node path
	 * @return The resolved node path
	 */
	String getNodePath( String root, String path ) {
		return PathUtil.normalize( PathUtil.isAbsolute( path ) ? path : PathUtil.resolve( root, path ) );
	}

	/**
	 * Dispatch the settings event to all value change handlers registered
	 * for the event's key.
	 *
	 * @param event The settings event to dispatch
	 */
	private void dispatch( SettingsEvent event ) {
		valueChangeHandlers.computeIfAbsent( event.getKey(), ( k ) -> new CopyOnWriteArraySet<>() ).forEach( h -> h.handle( event ) );
	}

	/**
	 * Converts a value to its marshalled string representation.
	 */
	private interface OutboundConverter {

		String convert( Object value );

	}

	/**
	 * Converts a marshalled string to its value.
	 */
	private interface InboundConverter {

		Object convert( String value );

	}

}
