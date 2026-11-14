package com.acromere.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Reads lines from a {@link Reader} on a background thread and exposes them
 * to the caller without blocking indefinitely.
 *
 * <p>A daemon reader thread continuously pulls lines from the underlying
 * source and places them in a queue. The consuming thread can then call
 * {@link #readLine(long, TimeUnit) readLine()} with a timeout to retrieve
 * available lines, making this useful for monitoring the output of
 * processes or streams while doing other work.</p>
 *
 * <p>The reader is not thread-safe and is intended to be consumed by a
 * single thread. It must be closed when no longer needed.</p>
 */
public class NonBlockingReader {

	private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();

	private final BufferedReader source;

	private Thread readerThread;

	private IOException ioexception;

	private boolean closed;

	/**
	 * Creates a reader for the given input stream, decoding content as
	 * UTF-8.
	 *
	 * @param input The input stream to read lines from, must not be
	 * {@code null}
	 */
	public NonBlockingReader( InputStream input ) {
		this( new InputStreamReader( input, StandardCharsets.UTF_8 ) );
	}

	/**
	 * Creates a reader for the given reader and starts the background
	 * reader thread.
	 *
	 * @param reader The reader to read lines from, must not be {@code null}
	 * @throws NullPointerException if the reader is {@code null}
	 */
	public NonBlockingReader( Reader reader ) {
		if( reader == null ) throw new NullPointerException( "Reader cannot be null" );
		source = (reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader( reader ));
		readerThread = new ReaderTask();
		readerThread.start();
	}

	/**
	 * Reads the next available line, waiting up to the given timeout.
	 *
	 * @param time The maximum time to wait
	 * @param unit The time unit of the {@code time} argument
	 * @return The next line, or {@code null} if no line became available
	 * within the timeout or the reader has been closed and all buffered
	 * lines have been consumed
	 * @throws IOException If an I/O error occurred in the background reader
	 * thread
	 */
	public String readLine( long time, TimeUnit unit ) throws IOException {
		if( closed && lines.isEmpty() ) return null;
		try {
			return lines.poll( time, unit );
		} catch( InterruptedException exception ) {
			if( ioexception != null ) throw ioexception;
			if( closed ) return null;
		}
		return null;
	}

	/**
	 * Closes the reader, interrupting the background reader thread.
	 *
	 * @throws IOException If an I/O error occurred in the background reader
	 * thread
	 */
	public void close() throws IOException {
		closed = true;
		if( readerThread != null ) readerThread.interrupt();
		if( ioexception != null ) throw ioexception;
		readerThread = null;
	}

	/**
	 * Background thread that reads lines from the source and enqueues them.
	 */
	private class ReaderTask extends Thread {

		ReaderTask() {
			super( "NonBlockingReaderThread" );
			setDaemon( true );
		}

		public void run() {
			try {
				while( !interrupted() ) {
					String line = source.readLine();
					if( line == null ) return;
					lines.add( line );
				}
			} catch( IOException exception ) {
				ioexception = exception;
			} finally {
				closed = true;
			}
		}

	}

}
