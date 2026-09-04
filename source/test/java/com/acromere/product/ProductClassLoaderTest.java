package com.acromere.product;

import com.acromere.util.OperatingSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductClassLoaderTest {

	@Test
	void testLoadClassFromParent() throws Exception {
		ClassLoader parent = getClass().getClassLoader();
		URI codebase = Path.of( "." ).toUri();
		try( ProductClassLoader loader = new ProductClassLoader( new URL[0], parent, codebase ) ) {
			Class<?> clazz = loader.loadClass( Contributor.class.getName() );
			assertThat( clazz ).isEqualTo( Contributor.class );
		}
	}

	@Test
	void testLoadClassThrowsClassNotFound() {
		ClassLoader parent = getClass().getClassLoader();
		URI codebase = Path.of( "." ).toUri();
		try( ProductClassLoader loader = new ProductClassLoader( new URL[0], parent, codebase ) ) {
			assertThatThrownBy( () -> loader.loadClass( "non.existent.Class" ) )
				.isInstanceOf( ClassNotFoundException.class );
		} catch( Exception e ) {
			// ignore on close
		}
	}

	@Test
	void testGetResourceFromParent() throws Exception {
		ClassLoader parent = getClass().getClassLoader();
		URI codebase = Path.of( "." ).toUri();
		try( ProductClassLoader loader = new ProductClassLoader( new URL[0], parent, codebase ) ) {
			URL resource = loader.getResource( "META-INF/product.card" );
			assertThat( resource ).isNotNull();

			URL nonExistent = loader.getResource( "non/existent/file.txt" );
			assertThat( nonExistent ).isNull();
		}
	}

	@Test
	void testFindLibrary(@TempDir Path tempDir) throws Exception {
		ClassLoader parent = getClass().getClassLoader();
		URI codebase = tempDir.toUri();

		try( ProductClassLoader loader = new ProductClassLoader( new URL[0], parent, codebase ) ) {
			// When library file doesn't exist
			String libName = "samplelib";
			String foundLib = loader.findLibrary( libName );
			assertThat( foundLib ).isNull();

			// When library file exists
			String nativePath = OperatingSystem.resolveNativeLibPath( libName );
			Path libFile = tempDir.resolve( "lib" ).resolve( nativePath );
			Files.createDirectories( libFile.getParent() );
			Files.writeString( libFile, "content" );

			foundLib = loader.findLibrary( libName );
			assertThat( foundLib ).isEqualTo( new File( codebase.resolve( "lib/" + nativePath ) ).toString() );
		}
	}

}
