package com.acromere.settings;

import com.acromere.util.FileUtil;
import com.acromere.util.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoredSettingsTest extends BaseSettingsTest {

	private static final String SETTINGS_NAME = "AcromereSettingsTest";

	private Path path;

	@BeforeEach
	void setup() throws Exception {
		path = FileUtil.createTempFolder( SETTINGS_NAME );
		settings = new StoredSettings( path );
	}

	@AfterEach
	void cleanup() throws Exception {
		settings.delete();
		if( Files.exists( path ) ) throw new IllegalStateException( "File still exists: " + path );
		Thread.sleep( 100 );
		if( Files.exists( path ) ) throw new IllegalStateException( "File came back after delete: " + path );
	}

	@Test
	void testGetNodesFromFolder() throws IOException {
		assertThat( settings.getNodes().size() ).isEqualTo( 0 );
		Path childFolder = path.resolve( "children" );
		Path childSettings = childFolder.resolve( "settings.properties" );
		Files.createDirectory( childFolder );
		Files.createFile( childSettings );
		assertThat( Files.exists( childSettings ) ).isTrue();
		assertThat( settings.getNodes().size() ).isEqualTo( 1 );
	}

	@Test
	void testSaveAfterDelete() {
		settings.set( "test", "1" );
		assertThat( Files.exists( path ) ).isTrue();
		settings.delete();
		assertThat( Files.exists( path ) ).isFalse();
		settings.set( "test", "2" );
		settings.flush();
		assertThat( Files.exists( path ) ).isFalse();
	}

	@Test
	void testConstructorWithSettingsFileNameThrowsException() {
		Path invalidFolder = path.resolve( "settings.properties" );
		assertThatThrownBy( () -> new StoredSettings( invalidFolder ) )
			.isInstanceOf( RuntimeException.class )
			.hasMessageContaining( "Folder ends with settings.properties" );
	}

	@Test
	void testFlushLimits() {
		StoredSettings storedSettings = (StoredSettings)settings;

		assertThat( storedSettings.getMinFlushLimit() ).isEqualTo( 1000 );
		assertThat( storedSettings.getMaxFlushLimit() ).isEqualTo( 5000 );

		storedSettings.setMinFlushLimit( 500 );
		storedSettings.setMaxFlushLimit( 2500 );

		assertThat( storedSettings.getMinFlushLimit() ).isEqualTo( 500 );
		assertThat( storedSettings.getMaxFlushLimit() ).isEqualTo( 2500 );
	}

	@Test
	void testToString() {
		assertThat( settings.toString() ).isEqualTo( path.toString() );
	}

	@Test
	void testJsonFilePersistenceAndRemoval() {
		MockBean bean = new MockBean();
		bean.setIntegerPrimitiveProperty( 77 );
		bean.setStringProperty( "persisted" );

		settings.set( "myBean", bean );
		Path jsonFile = path.resolve( "myBean.json" );
		assertThat( Files.exists( jsonFile ) ).isTrue();

		// Check reloading from fresh StoredSettings instance
		StoredSettings freshSettings = new StoredSettings( path );
		MockBean loadedBean = freshSettings.get( "myBean", MockBean.class );
		assertThat( loadedBean ).isEqualTo( bean );

		// Check removing key removes json file
		settings.remove( "myBean" );
		assertThat( Files.exists( jsonFile ) ).isFalse();
		assertThat( settings.get( "myBean", MockBean.class ) ).isNull();
	}

	@Test
	void testJsonCollectionsAndArraysPersistence() {
		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );

		// List
		settings.set( "listKey", List.of( bean1, bean2 ) );
		assertThat( Files.exists( path.resolve( "listKey.json" ) ) ).isTrue();

		// Set
		settings.set( "setKey", Set.of( bean1, bean2 ) );
		assertThat( Files.exists( path.resolve( "setKey.json" ) ) ).isTrue();

		// Map
		settings.set( "mapKey", Map.of( "b1", bean1, "b2", bean2 ) );
		assertThat( Files.exists( path.resolve( "mapKey.json" ) ) ).isTrue();

		// Array
		settings.set( "arrayKey", new MockBean[]{ bean1, bean2 } );
		assertThat( Files.exists( path.resolve( "arrayKey.json" ) ) ).isTrue();

		// Reload from fresh instance
		StoredSettings reloaded = new StoredSettings( path );
		List<MockBean> loadedList = reloaded.get( "listKey", new TypeReference<>() {} );
		assertThat( loadedList ).containsExactly( bean1, bean2 );

		Set<MockBean> loadedSet = reloaded.get( "setKey", new TypeReference<>() {} );
		assertThat( loadedSet ).containsExactlyInAnyOrder( bean1, bean2 );

		Map<String, MockBean> loadedMap = reloaded.get( "mapKey", new TypeReference<>() {} );
		assertThat( loadedMap ).containsEntry( "b1", bean1 ).containsEntry( "b2", bean2 );

		MockBean[] loadedArray = reloaded.get( "arrayKey", MockBean[].class );
		assertThat( loadedArray ).containsExactly( bean1, bean2 );
	}

	@Test
	void testSavedEventOnFlush() throws Exception {
		CountDownLatch latch = new CountDownLatch( 1 );
		settings.register( SettingsEvent.SAVED, event -> latch.countDown() );

		settings.set( "flushKey", "flushVal" );
		settings.flush();

		assertThat( latch.await( 2, TimeUnit.SECONDS ) ).isTrue();
	}

	@Test
	void testLoadedEventOnInitialPropertiesLoad() throws Exception {
		Path subFolder = path.resolve( "subWithProps" );
		Files.createDirectories( subFolder );
		Path propFile = subFolder.resolve( "settings.properties" );
		Files.writeString( propFile, "testProp=testVal\n" );

		StoredSettings subSettings = new StoredSettings( subFolder );
		assertThat( subSettings.get( "testProp" ) ).isEqualTo( "testVal" );
	}

	@Test
	void testGetNodesWithIntermediateParents() {
		Settings deep = settings.getNode( "/a/b/c" );
		deep.set( "k", "v" );

		assertThat( settings.nodeExists( "a" ) ).isTrue();
		assertThat( settings.nodeExists( "a/b" ) ).isTrue();
		assertThat( settings.nodeExists( "a/b/c" ) ).isTrue();

		assertThat( settings.getNodes() ).contains( "a" );
		assertThat( settings.getNode( "a" ).getNodes() ).contains( "b" );
		assertThat( settings.getNode( "a/b" ).getNodes() ).contains( "c" );
	}

}
