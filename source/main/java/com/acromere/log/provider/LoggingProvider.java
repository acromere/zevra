package com.acromere.log.provider;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface LoggingProvider {

	static long getCurrentTimeNanos() {
		return MILLISECONDS.toNanos( System.currentTimeMillis() );
	}

	LoggerWrapper getLoggerWrapper( String name );

}
