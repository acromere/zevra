package com.acromere.util;

import lombok.CustomLog;

import java.io.*;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.*;

/**
 * A class to work with files and folders, including saving and loading
 * text, copying and moving, zipping and unzipping, calculating sizes, and
 * managing temporary files.
 */
@CustomLog
public class FileUtil {

	/**
	 * The system temporary folder.
	 */
	public static final Path TEMP_FOLDER = Paths.get( System.getProperty( "java.io.tmpdir" ) );

	private static final long FILE_COPY_BUFFER_SIZE = SizeUnitBase2.MiB.getSize() * 25;

	/**
	 * Get a human-readable string using orders of magnitude in base-10.
	 *
	 * @param size The size to convert to human-readable
	 * @return The human-readable size string
	 */
	public static String getHumanSize( long size ) {
		return getHumanSize( size, false );
	}

	/**
	 * Get a human-readable string using orders of magnitude in base-10.
	 *
	 * @param size The size to convert to human-readable
	 * @param compact If the unit should use compact notation
	 * @return The human-readable size string
	 */
	public static String getHumanSize( long size, boolean compact ) {
		return getHumanSize( size, compact, true );
	}

	/**
	 * Get a human-readable string using orders of magnitude in base-10.
	 *
	 * @param size The size to convert to human-readable
	 * @param compact If the unit should use compact notation
	 * @param showUnit If the unit should be shown
	 * @return The human-readable size string
	 */
	public static String getHumanSize( long size, boolean compact, boolean showUnit ) {
		int exponent = 0;
		boolean negative = size < 0;
		size = Math.abs( size );
		long coefficient = size;
		long base = SizeUnitBase10.KB.getSize();
		while( coefficient >= base ) {
			coefficient /= base;
			exponent++;
		}

		SizeUnitBase10 unit = SizeUnitBase10.values()[ exponent ];
		String unitString = compact ? unit.getCompact() : unit.name();

		// Should be, at most, seven characters long: possible negative sign, four numbers, two units
		StringBuilder text = new StringBuilder( negative ? "-" : "" );
		if( exponent > 0 && coefficient < 10 ) {
			long precise = size;
			while( precise >= SizeUnitBase10.MB.getSize() ) {
				precise /= base;
			}
			text.append( String.format( "%3.1f", (float)precise / base ) );
		} else {
			text.append( coefficient );
		}
		if( showUnit ) {
			if( !compact ) text.append( " " );
			text.append( unitString );
		}

		return text.toString();
	}

	/**
	 * Get a human-readable string using orders of magnitude in base-2.
	 *
	 * @param size The size to convert to human-readable
	 * @return The human-readable size string
	 */
	public static String getHumanSizeBase2( long size ) {
		return getHumanSizeBase2( size, false );
	}

	/**
	 * Get a human-readable string using orders of magnitude in base-2.
	 *
	 * @param size The size to convert to human-readable
	 * @param compact If the unit should use compact notation
	 * @return The human-readable size string
	 */
	public static String getHumanSizeBase2( long size, boolean compact ) {
		return getHumanSizeBase2( size, compact, true );
	}

	/**
	 * Get a human-readable string using orders of magnitude in base-2.
	 *
	 * @param size The size to convert to human-readable
	 * @param compact If the unit should use compact notation
	 * @param showUnit If the unit should be shown
	 * @return The human-readable size string
	 */
	public static String getHumanSizeBase2( long size, boolean compact, boolean showUnit ) {
		int exponent = 0;
		boolean negative = size < 0;
		size = Math.abs( size );
		long coefficient = size;
		long base = SizeUnitBase2.KiB.getSize();
		while( coefficient >= base ) {
			coefficient /= base;
			exponent++;
		}

		SizeUnitBase2 unit = SizeUnitBase2.values()[ exponent ];
		String unitString = compact ? unit.getCompact() : unit.name();

		// Should be, at most, eight characters long: possible negative sign, four numbers, three units
		StringBuilder text = new StringBuilder( negative ? "-" : "" );
		if( exponent > 0 && coefficient < 10 ) {
			long precise = size;
			while( precise >= SizeUnitBase2.MiB.getSize() ) {
				precise /= base;
			}
			text.append( String.format( "%3.1f", (float)precise / base ) );
		} else {
			text.append( coefficient );
		}
		if( showUnit ) {
			if( !compact ) text.append( " " );
			text.append( unitString );
		}

		return text.toString();
	}

	/**
	 * Returns the extension of the file name of the given path.
	 *
	 * @param path The path from which to get the extension
	 * @return The extension, or an empty string if there is none, or null
	 *         if the path is null
	 * @see #getExtension( String )
	 */
	public static String getExtension( Path path ) {
		return path == null ? null : getExtension( path.getFileName().toString() );
	}

	/**
	 * Returns the extension of a file name, that is the part of the name
	 * after the last period.
	 *
	 * @param name The file name from which to get the extension
	 * @return The extension, or an empty string if there is none, or null if
	 *         the name is null
	 */
	public static String getExtension( String name ) {
		if( name == null ) return null;
		int index = name.lastIndexOf( '.' );
		if( index < 0 ) return "";
		return name.substring( index + 1 );
	}

	/**
	 * Returns a path with the extension removed from the file name.
	 *
	 * @param path The path from which to remove the extension
	 * @return The path without the extension, or null if the path is null
	 * @see #removeExtension( String )
	 */
	public static Path removeExtension( Path path ) {
		return path == null ? null : Paths.get( removeExtension( path.toString() ) );
	}

	/**
	 * Returns a file name with the extension removed.
	 *
	 * @param name The file name from which to remove the extension
	 * @return The file name without the extension, or null if the name is null
	 */
	public static String removeExtension( String name ) {
		if( name == null ) return null;
		int index = name.lastIndexOf( '.' );
		if( index < 0 ) return name;
		return name.substring( 0, index );
	}

	/**
	 * Save a string to a file using UTF-8 encoding.
	 *
	 * @param data The string to save
	 * @param target The file to save to
	 * @throws IOException If an I/O error occurs
	 * @see #save( String, Path, Charset )
	 */
	public static void save( String data, Path target ) throws IOException {
		save( data, target, StandardCharsets.UTF_8 );
	}

	/**
	 * Save a string to a file using the given encoding name.
	 *
	 * @param data The string to save
	 * @param target The file to save to
	 * @param encoding The name of the encoding to use
	 * @throws IOException If an I/O error occurs
	 * @see #save( String, Path, Charset )
	 */
	public static void save( String data, Path target, String encoding ) throws IOException {
		save( data, target, Charset.forName( encoding ) );
	}

	/**
	 * Save a string to a file using the given encoding.
	 *
	 * @param data The string to save
	 * @param target The file to save to
	 * @param encoding The encoding to use
	 * @throws IOException If an I/O error occurs
	 */
	public static void save( String data, Path target, Charset encoding ) throws IOException {
		try( OutputStream output = new FileOutputStream( target.toFile() ) ) {
			IoUtil.write( data, output, encoding );
		}
	}

	/**
	 * Load a file into a string using UTF-8 encoding.
	 *
	 * @param source The file to load
	 * @return The file contents as a string
	 * @throws IOException If an I/O error occurs
	 * @see #load( Path, Charset )
	 */
	public static String load( Path source ) throws IOException {
		return load( source, StandardCharsets.UTF_8 );
	}

	/**
	 * Load a file into a string using the given encoding name.
	 *
	 * @param source The file to load
	 * @param encoding The name of the encoding to use
	 * @return The file contents as a string
	 * @throws IOException If an I/O error occurs
	 * @see #load( Path, Charset )
	 */
	public static String load( Path source, String encoding ) throws IOException {
		return load( source, Charset.forName( encoding ) );
	}

	/**
	 * Load a file into a string using the given encoding.
	 *
	 * @param source The file to load
	 * @param encoding The encoding to use
	 * @return The file contents as a string
	 * @throws IOException If an I/O error occurs
	 */
	public static String load( Path source, Charset encoding ) throws IOException {
		try( FileInputStream input = new FileInputStream( source.toString() ) ) {
			return IoUtil.toString( input, encoding );
		}
	}

	/**
	 * Copy a file or folder to the target, not including the root folder
	 * of a folder source.
	 *
	 * @param source The file or folder to copy
	 * @param target The file or folder to copy to
	 * @return true if the copy succeeded, false otherwise
	 * @throws IOException If an I/O error occurs
	 * @see #copy( Path, Path, boolean, LongConsumer )
	 */
	public static boolean copy( Path source, Path target ) throws IOException {
		return copy( source, target, false );
	}

	/**
	 * Copy a file or folder to the target, not including the root folder
	 * of a folder source, reporting progress to the given callback.
	 *
	 * @param source The file or folder to copy
	 * @param target The file or folder to copy to
	 * @param progressCallback The callback to report the number of bytes copied to, or null
	 * @return true if the copy succeeded, false otherwise
	 * @throws IOException If an I/O error occurs
	 * @see #copy( Path, Path, boolean, LongConsumer )
	 */
	public static boolean copy( Path source, Path target, LongConsumer progressCallback ) throws IOException {
		return copy( source, target, false, progressCallback );
	}

	/**
	 * Copy a file or folder to the target.
	 *
	 * @param source The file or folder to copy
	 * @param target The file or folder to copy to
	 * @param includeRootFolder If the root folder of a folder source should be included in the target
	 * @return true if the copy succeeded, false otherwise
	 * @throws IOException If an I/O error occurs
	 * @see #copy( Path, Path, boolean, LongConsumer )
	 */
	public static boolean copy( Path source, Path target, boolean includeRootFolder ) throws IOException {
		return copy( source, target, includeRootFolder, null );
	}

	/**
	 * Copy a file or folder to the target.
	 *
	 * <p>
	 * A file source is copied to a file target, to a file with the same name
	 * inside a folder target, or to a new file target. A folder source is
	 * copied to a folder target; if <code>includeRootFolder</code> is true,
	 * the root folder of the source is included in the target.
	 * </p>
	 *
	 * @param source The file or folder to copy
	 * @param target The file or folder to copy to
	 * @param includeRootFolder If the root folder of a folder source should be included in the target
	 * @param progressCallback The callback to report the number of bytes copied to, or null
	 * @return true if the copy succeeded, false otherwise
	 * @throws IOException If an I/O error occurs
	 */
	public static boolean copy( Path source, Path target, boolean includeRootFolder, LongConsumer progressCallback ) throws IOException {
		return copy( source.toFile(), target.toFile(), includeRootFolder, progressCallback );
	}

	/**
	 * Copy the contents of a file to an output stream.
	 *
	 * @param file The file to copy
	 * @param target The output stream to copy to
	 * @return The number of bytes copied
	 * @throws IOException If an I/O error occurs
	 */
	public static long copy( Path file, OutputStream target ) throws IOException {
		try( FileInputStream source = new FileInputStream( file.toFile() ) ) {
			return IoUtil.copy( source, target );
		}
	}

	/**
	 * Copy a file or folder to the target.
	 *
	 * @param source The file or folder to copy
	 * @param target The file or folder to copy to
	 * @param includeRootFolder If the root folder of a folder source should be included in the target
	 * @return true if the copy succeeded, false otherwise
	 * @throws IOException If an I/O error occurs
	 * @see #copy( File, File, boolean, LongConsumer )
	 */
	public static boolean copy( File source, File target, boolean includeRootFolder ) throws IOException {
		return copy( source, target, includeRootFolder, null );
	}

	/**
	 * Copy a file or folder to the target.
	 *
	 * <p>
	 * A file source is copied to a file target, to a file with the same name
	 * inside a folder target, or to a new file target. A folder source is
	 * copied to a folder target; if <code>includeRootFolder</code> is true,
	 * the root folder of the source is included in the target.
	 * </p>
	 *
	 * @param source The file or folder to copy
	 * @param target The file or folder to copy to
	 * @param includeRootFolder If the root folder of a folder source should be included in the target
	 * @param progressCallback The callback to report the number of bytes copied to, or null
	 * @return true if the copy succeeded, false otherwise
	 * @throws IOException If an I/O error occurs
	 */
	public static boolean copy( File source, File target, boolean includeRootFolder, LongConsumer progressCallback ) throws IOException {
		// Copy file sources to file targets
		if( source.isFile() && target.isFile() ) {
			copyFileToFile( source, target, progressCallback );
			return true;
		}

		// Copy file sources to folder targets
		if( source.isFile() && target.isDirectory() ) {
			copyFileToDirectory( source, target, progressCallback );
			return true;
		}

		// Copy folder sources to folder targets
		if( source.isDirectory() && target.isDirectory() ) {
			if( includeRootFolder ) {
				copyDirectoryToDirectory( source, target, progressCallback );
			} else {
				copyDirectory( source, target, progressCallback );
			}
			return true;
		}

		// Copy file source to new file target
		if( source.isFile() ) {
			copyFileToFile( source, target, progressCallback );
			return true;
		}

		return false;
	}

	private static void copyFileToFile( File source, File target, LongConsumer progressCallback ) throws IOException {
		if( target.exists() && target.isDirectory() ) throw new IOException( "Destination is a directory: " + target );

		try( FileInputStream fis = new FileInputStream( source ); FileChannel input = fis.getChannel(); FileOutputStream fos = new FileOutputStream( target ); FileChannel output = fos.getChannel() ) {
			final long size = input.size();
			long count;
			long position = 0;
			while( position < size ) {
				final long remain = size - position;
				count = Math.min( remain, FILE_COPY_BUFFER_SIZE );
				final long bytesCopied = output.transferFrom( input, position, count );
				if( bytesCopied == 0 ) break;
				position += bytesCopied;
				if( progressCallback != null ) progressCallback.accept( position );
			}
		}
	}

	private static void copyFileToDirectory( File source, File target, LongConsumer progressCallback ) throws IOException {
		copyFileToFile( source, new File( target, source.getName() ), progressCallback );
	}

	private static void copyDirectoryToDirectory( File source, File target, LongConsumer progressCallback ) throws IOException {
		copyDirectory( source, new File( target, source.getName() ), progressCallback );
	}

	private static void copyDirectory( File source, File target, LongConsumer progressCallback ) throws IOException {
		doCopyDirectory( source, target, null, progressCallback );
	}

	private static void doCopyDirectory( File source, File target, FileFilter filter, LongConsumer progressCallback ) throws IOException {
		File[] sourceFiles = filter == null ? source.listFiles() : source.listFiles( filter );
		if( sourceFiles == null ) throw new IOException( "Failed to list source: " + source );

		// Make sure there is a valid target
		if( target.exists() ) {
			if( !target.isDirectory() ) throw new IOException( "Target not a folder: " + target );
		} else {
			if( !target.mkdirs() && !target.isDirectory() ) throw new IOException( "Cannot create target: " + target );
		}
		if( !target.canWrite() ) throw new IOException( "Cannot write to target: " + target );

		// Copy files
		for( File sourceFile : sourceFiles ) {
			File targetFile = new File( target, sourceFile.getName() );
			if( sourceFile.isDirectory() ) {
				doCopyDirectory( sourceFile, targetFile, filter, progressCallback );
			} else {
				copyFileToFile( sourceFile, targetFile, progressCallback );
			}
		}
	}

	/**
	 * Move a file or folder to the target path.
	 *
	 * <p>
	 * An atomic move is attempted first; if that fails, the source is
	 * copied to the target and deleted instead, and this method returns
	 * true only if that fallback succeeded.
	 * </p>
	 *
	 * @param source The file or folder to move
	 * @param target The new location
	 * @return true if the copy-and-delete fallback succeeded, false otherwise
	 * @throws IOException If an I/O error occurs
	 */
	public static boolean move( Path source, Path target ) throws IOException {
		try {
			Files.move( source, target, StandardCopyOption.ATOMIC_MOVE );
		} catch( IOException exception ) {
			if( copy( source, target ) && delete( source ) ) return true;
		}
		return false;
	}

	/**
	 * Zip the contents of a file or folder into a zip file.
	 *
	 * @param source The file or folder to zip
	 * @param target The zip file to write
	 * @throws IOException If an I/O error occurs
	 * @see #zip( Path, Path, LongConsumer )
	 */
	public static void zip( Path source, Path target ) throws IOException {
		zip( source, target, null );
	}

	/**
	 * Unzip a zip file into a target folder.
	 *
	 * @param source The zip file to unzip
	 * @param target The folder to unzip into
	 * @throws IOException If an I/O error occurs
	 * @see #unzip( Path, Path, LongConsumer )
	 */
	public static void unzip( Path source, Path target ) throws IOException {
		unzip( source, target, null );
	}

	/**
	 * Zip the contents of a file or folder into a zip file.
	 *
	 * <p>
	 * Entries are stored relative to the source, with folders given a
	 * trailing slash.
	 * </p>
	 *
	 * @param source The file or folder to zip
	 * @param target The zip file to write
	 * @param progressCallback The callback to report the total number of bytes added to, or null
	 * @throws IOException If an I/O error occurs
	 */
	public static void zip( Path source, Path target, LongConsumer progressCallback ) throws IOException {
		final AtomicLong total = new AtomicLong( 0 );
		final AtomicLong last = new AtomicLong( 0 );

		try( ZipOutputStream zip = new ZipOutputStream( new FileOutputStream( target.toFile() ) ) ) {
			for( Path sourcePath : listPaths( source ) ) {
				boolean folder = Files.isDirectory( sourcePath );
				String zipEntryPath = source.relativize( sourcePath ).toString();

				if( folder ) {
					// Folders need to have a trailing slash
					zip.putNextEntry( new ZipEntry( zipEntryPath + "/" ) );
				} else {
					zip.putNextEntry( new ZipEntry( zipEntryPath ) );
					try( FileInputStream input = new FileInputStream( sourcePath.toFile() ) ) {
						IoUtil.copy(
							input, zip, ( value ) -> {
								long diff = value - last.get();
								if( progressCallback != null && diff > 0 ) progressCallback.accept( total.addAndGet( diff ) );
								last.set( value );
							}
						);
					}
				}
				zip.closeEntry();
			}
		}
	}

	/**
	 * Unzip a zip file into a target folder.
	 *
	 * <p>
	 * Entry paths are sanitized, and an entry that would resolve outside
	 * of the target folder causes an {@link IOException}.
	 * </p>
	 *
	 * @param source The zip file to unzip
	 * @param target The folder to unzip into
	 * @param progressCallback The callback to report the number of bytes read from each entry to, or null
	 * @throws IOException If an I/O error occurs, or an entry path escapes the target folder
	 */
	public static void unzip( Path source, Path target, LongConsumer progressCallback ) throws IOException {
		Files.createDirectories( target );
		Path normalizedTarget = target.toAbsolutePath().normalize();

		ZipEntry entry;
		try( ZipInputStream zip = new ZipInputStream( new FileInputStream( source.toFile() ) ) ) {
			while( (entry = zip.getNextEntry()) != null ) {
				String path = sanitize( entry.getName() );
				if( TextUtil.isEmpty( path ) ) continue;

				Path file = normalizedTarget.resolve( path ).normalize();
				if( !file.startsWith( normalizedTarget ) ) {
					throw new IOException( "Bad zip entry path: " + entry.getName() );
				}

				if( path.endsWith( "/" ) ) {
					Files.createDirectories( file );
				} else {
					Files.createDirectories( file.getParent() );
					try( FileOutputStream output = new FileOutputStream( file.toFile() ) ) {
						IoUtil.copy( zip, output, progressCallback );
					}
				}
			}
		}
	}

	/**
	 * Get the total size of a file, or of all the files in a folder,
	 * including any nested folders.
	 *
	 * @param source The file or folder to calculate the size of
	 * @return The total size in bytes
	 * @throws IOException If an I/O error occurs
	 */
	public static long getDeepSize( Path source ) throws IOException {
		List<Path> paths = listPaths( source );

		long total = 0;
		for( Path path : paths ) {
			total += Files.isDirectory( path ) ? 0 : Files.size( path );
		}

		return total;
	}

	/**
	 * Get the total uncompressed size of the entries in a zip file.
	 *
	 * @param source The zip file to calculate the size of
	 * @return The total uncompressed size in bytes
	 * @throws IOException If an I/O error occurs
	 */
	public static long getUncompressedSize( Path source ) throws IOException {
		try( ZipFile zipFile = new ZipFile( source.toFile() ) ) {
			Iterator<? extends ZipEntry> entries = zipFile.entries().asIterator();

			long total = 0;
			while( entries.hasNext() ) {
				ZipEntry entry = entries.next();
				total += entry.isDirectory() ? 0 : entry.getSize();
			}

			return total;
		}
	}

	/**
	 * Get the total uncompressed size of a file or folder, recursing
	 * into nested folders and expanding any zip files found.
	 *
	 * <p>
	 * Files that are not valid zip files contribute their on-disk size.
	 * </p>
	 *
	 * @param source The file or folder to calculate the size of
	 * @return The total uncompressed size in bytes
	 * @throws IOException If an I/O error occurs
	 */
	@SuppressWarnings( "unused" )
	public static long getDeepUncompressedSize( Path source ) throws IOException {
		List<Path> paths = listPaths( source );

		long total = 0;
		for( Path path : paths ) {
			if( Files.isDirectory( path ) ) {
				total += getDeepUncompressedSize( path );
			} else {
				try {
					total += getUncompressedSize( path );
				} catch( ZipException exception ) {
					total += Files.size( path );
				}
			}
		}

		return total;
	}

	/**
	 * List the given path and all of its nested paths.
	 *
	 * @param file The file or folder to list
	 * @return A list of all paths, starting with the given path
	 * @throws IOException If an I/O error occurs
	 */
	public static List<Path> listPaths( Path file ) throws IOException {
		try( Stream<Path> paths = Files.walk( file ) ) {
			return paths.collect( Collectors.toList() );
		}
	}

	/**
	 * Delete a file or folder, recursively, including all nested files
	 * and folders.
	 *
	 * @param path The path to delete
	 * @return true if the path no longer exists, including if it did not
	 * exist in the first place
	 * @throws IOException If an I/O error occurs
	 */
	public static boolean delete( Path path ) throws IOException {
		if( !Files.exists( path ) ) return true;

		Files.walkFileTree(
			path, new SimpleFileVisitor<>() {

				@Override
				public FileVisitResult visitFile( Path file, BasicFileAttributes attributes ) throws IOException {
					Files.delete( file );
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory( Path folder, IOException exception ) throws IOException {
					if( exception != null ) throw exception;

					Files.delete( folder );
					return FileVisitResult.CONTINUE;
				}
			}
		);

		return !Files.exists( path );
	}

	/**
	 * Register a file or folder for deletion when the JVM exits,
	 * recursively, including all nested files and folders.
	 *
	 * @param path The path to register for deletion
	 * @throws IOException If an I/O error occurs
	 */
	public static void deleteOnExit( Path path ) throws IOException {
		if( !Files.exists( path ) ) return;

		Files.walkFileTree(
			path, new SimpleFileVisitor<>() {

				@Override
				public FileVisitResult visitFile( Path file, BasicFileAttributes attributes ) {
					file.toFile().deleteOnExit();
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory( Path folder, IOException exception ) throws IOException {
					if( exception == null ) {
						folder.toFile().deleteOnExit();
						return FileVisitResult.CONTINUE;
					} else {
						throw exception;
					}
				}
			}
		);
	}

	/**
	 * Get the system temporary folder.
	 *
	 * @return The system temporary folder
	 */
	public static Path getTempFolder() {
		return TEMP_FOLDER;
	}

	/**
	 * Create a temporary file in the default temporary-file directory.
	 *
	 * @param prefix The prefix of the file name
	 * @param suffix The suffix of the file name
	 * @param attributes The attributes of the file to create
	 * @return The path to the newly created file
	 * @throws IOException If an I/O error occurs
	 */
	public static Path createTempFile( String prefix, String suffix, FileAttribute<?>... attributes ) throws IOException {
		return Files.createTempFile( prefix, suffix, attributes );
	}

	/**
	 * Create a temporary file in the given parent folder.
	 *
	 * @param parent The parent folder in which to create the file
	 * @param prefix The prefix of the file name
	 * @param suffix The suffix of the file name
	 * @param attributes The attributes of the file to create
	 * @return The path to the newly created file
	 * @throws IOException If an I/O error occurs
	 */
	public static Path createTempFile( Path parent, String prefix, String suffix, FileAttribute<?>... attributes ) throws IOException {
		return Files.createTempFile( parent, prefix, suffix, attributes );
	}

	/**
	 * Create a temporary folder.
	 *
	 * @param prefix The temp folder prefix
	 * @return The temp folder Path object
	 * @throws IOException If an I/O error occurs
	 */
	public static Path createTempFolder( String prefix ) throws IOException {
		return Files.createTempDirectory( prefix );
	}

	/**
	 * Create a temporary folder.
	 *
	 * @param path The parent folder in which to create the temporary folder
	 * @param prefix The temp folder prefix
	 * @return The temp folder Path object
	 * @throws IOException If an I/O error occurs
	 */
	public static Path createTempFolder( Path path, String prefix ) throws IOException {
		return Files.createTempDirectory( path, prefix );
	}

	/**
	 * Create a temporary folder.
	 *
	 * @param prefix The temporary folder prefix
	 * @param suffix The suffix of the folder name
	 * @return A temporary folder path
	 * @throws IOException If an error occurs
	 */
	public static Path createTempFolder( String prefix, String suffix ) throws IOException {
		Path path = Files.createTempFile( prefix, suffix );
		Files.deleteIfExists( path );
		if( Files.exists( path ) ) throw new IOException( "Unable to create temp folder" );
		Files.createDirectories( path );
		if( !Files.exists( path ) ) throw new IOException( "Unable to create temp folder" );
		return path;
	}

	/**
	 * Create a temporary folder.
	 *
	 * @param parent The parent folder of the temporary folder
	 * @param prefix The temporary folder prefix
	 * @param suffix The suffix of the folder name
	 * @return A temporary folder path
	 * @throws IOException If an error occurs
	 */
	public static Path createTempFolder( Path parent, String prefix, String suffix ) throws IOException {
		Path path = Files.createTempFile( parent, prefix, suffix );
		Files.deleteIfExists( path );
		if( Files.exists( path ) ) throw new IOException( "Unable to create temp folder" );
		Files.createDirectories( path );
		if( !Files.exists( path ) ) throw new IOException( "Unable to create temp folder" );
		return path;
	}

	/**
	 * Find the closest existing parent folder for the given file.
	 *
	 * @param file The file for which to find a valid parent folder
	 * @return The closest existing parent folder, or null if the file is null
	 *         or no existing parent folder is found
	 */
	public static Path findValidParent( String file ) {
		if( file == null ) return null;
		return findValidParent( Paths.get( file ) );
	}

	/**
	 * Find the closest existing parent folder for the given file.
	 *
	 * @param file The file for which to find a valid parent folder
	 * @return The closest existing parent folder, or null if the file is null
	 *         or no existing parent folder is found
	 */
	public static Path findValidParent( File file ) {
		if( file == null ) return null;
		return findValidParent( file.toPath() );
	}

	/**
	 * Find the closest existing parent folder for the given path.
	 *
	 * @param path The path for which to find a valid parent
	 * @return The closest existing parent folder, or null if the path is null
	 *         or no existing parent folder is found
	 */
	public static Path findValidParent( Path path ) {
		if( path == null ) return null;
		return findValidFolder( path.getParent() );
	}

	/**
	 * Find the closest existing folder for the given path.
	 *
	 * @param path The path for which to find a valid parent
	 * @return The closest existing folder, or null if the path is null or no
	 *         existing folder is found
	 */
	public static Path findValidFolder( String path ) {
		if( path == null ) return null;

		// Fix Windows paths
		path = path.replace( '\\', '/' );

		if( path.startsWith( "file:" ) ) return findValidFolder( new File( URI.create( path ) ) );

		return findValidFolder( new File( path ) );
	}

	/**
	 * Find the closest existing folder for the given file.
	 *
	 * @param file The file for which to find a valid parent
	 * @return The closest existing folder, or null if the file is null or no
	 *         existing folder is found
	 */
	public static Path findValidFolder( File file ) {
		if( file == null ) return null;
		return findValidFolder( file.toPath() );
	}

	/**
	 * Find the closest existing folder for the given path.
	 *
	 * @param path The path for which to find a valid parent
	 * @return The closest existing folder, or null if the path is null or no
	 *         existing folder is found
	 */
	public static Path findValidFolder( Path path ) {
		while( path != null && Files.notExists( path ) | !Files.isDirectory( path ) ) {
			path = path.getParent();
		}
		return path;
	}

	/**
	 * Get a name that does not collide with the given existing names.
	 *
	 * <p>
	 * If the name is not already in use, it is returned unchanged;
	 * otherwise a numeric index is appended to the base name before
	 * the extension, for example <code>a.txt</code> becomes
	 * <code>a(1).txt</code>. The index is one greater than the highest
	 * index already present among the existing names.
	 * </p>
	 *
	 * @param existingNames The names that are already in use
	 * @param name The name to index
	 * @return The name, or a new name with an appended index
	 */
	public static String getNextIndexedName( List<String> existingNames, String name ) {
		String extension = FileUtil.getExtension( name );
		String base = FileUtil.removeExtension( name );

		String startDelim = "(";
		String endDelim = ")";

		String regex = "^" + base + "\\" + startDelim + "\\d+\\" + endDelim + (extension.isEmpty() ? "" : "\\." + extension) + "$";

		int index = -1;
		for( String existing : existingNames ) {
			if( existing.matches( regex ) ) {
				int start = existing.indexOf( startDelim, base.length() ) + 1;
				int end = existing.indexOf( endDelim, start );
				index = Integer.parseInt( existing.substring( start, end ) );
			} else {
				if( existing.equals( name ) ) index = 0;
			}
		}

		if( index < 0 ) return name;

		base = base + "(" + (index + 1) + ")";
		return base + (extension.isEmpty() ? "" : "." + extension);
	}

	/**
	 * Wait until the given path exists.
	 *
	 * <p>
	 * The parent folder of the path is watched for entry creation events,
	 * so the parent folder must already exist.
	 * </p>
	 *
	 * @param path The path to wait for
	 * @param duration The maximum time to wait
	 * @param unit The unit of the duration
	 * @throws IOException If the parent folder does not exist, or an I/O error occurs
	 * @throws TimeoutException If the path did not appear within the given duration
	 * @throws InterruptedException If the wait is interrupted
	 */
	public static void waitToExist( Path path, long duration, TimeUnit unit ) throws IOException, TimeoutException, InterruptedException {
		if( Files.exists( path ) ) return;
		if( !Files.exists( path.getParent() ) ) throw new IOException( "Cannot wait for path without parent: " + path );

		long maxDuration = unit.toMillis( duration );
		long checkDelay = 100;

		try( WatchService watcher = FileSystems.getDefault().newWatchService() ) {
			path.getParent().register( watcher, java.nio.file.StandardWatchEventKinds.ENTRY_CREATE );
			if( Files.exists( path ) ) return;
			WatchKey key;
			long checkDuration = 0;
			while( checkDuration < maxDuration ) {
				key = watcher.poll( checkDelay, TimeUnit.MILLISECONDS );
				if( key != null ) {
					for( WatchEvent<?> event : key.pollEvents() ) {
						if( event.kind() == StandardWatchEventKinds.ENTRY_CREATE ) {
							if( event.context().equals( path.getFileName() ) ) {
								return;
							}
						}
					}
					key.reset();
				}
				checkDuration += checkDelay;
			}
			throw new TimeoutException( "Timed out waiting for path to exist: " + path );
		}
	}

	private static String sanitize( String path ) {
		boolean found;
		do {
			found = false;
			if( path.startsWith( "/" ) ) {
				// Remove leading slashes
				path = path.substring( 1 );
				found = true;
			} else if( path.startsWith( "../" ) ) {
				// Remove leading "../"
				path = path.substring( 3 );
				found = true;
			}
		} while( found );
		return path;
	}

}
