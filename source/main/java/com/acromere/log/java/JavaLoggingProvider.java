package com.acromere.log.java;

import com.acromere.log.provider.AbstractLoggingProvider;
import com.acromere.log.provider.LoggerWrapper;

public class JavaLoggingProvider extends AbstractLoggingProvider {

	public LoggerWrapper getLoggerWrapper( String name ) {
		return new JavaLoggingLoggerWrapper( java.util.logging.Logger.getLogger( name ) );
	}

}
