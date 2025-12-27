package com.acromere.log.provider;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface LoggingProvider {

	LoggerWrapper getLoggerWrapper( String name );

	static long getCurrentTimeNanos() {
		return MILLISECONDS.toNanos( System.currentTimeMillis() );
	}

}
