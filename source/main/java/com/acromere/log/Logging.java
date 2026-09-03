package com.acromere.log;

import com.acromere.log.java.JavaLoggingProvider;
import com.acromere.log.provider.LoggingProvider;

public class Logging {

	private static LoggingProvider provider = new JavaLoggingProvider();

	public static LoggingProvider getLoggingProvider() {
		return provider;
	}

	public static void setLoggingProvider( LoggingProvider provider ) {
		Logging.provider = provider;
	}

	public static Logger create( java.lang.Class<?> clazz ) {
		return new Logger( getLoggingProvider().getLoggerWrapper( clazz.getName() ) );
	}

}
