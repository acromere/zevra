package com.acromere.log.provider;

import java.util.logging.Level;

/**
 * Abstract base class for logger wrappers.
 * <p>
 * Provides default no-op implementations for {@link #getLoggerName()} and
 * {@link #isLoggable(Level)}. Subclasses must implement {@link #log(LogData)},
 * {@link #handleError(LogData, RuntimeException)}, and {@link #flush()}.
 */
public abstract class AbstractLoggerWrapper implements LoggerWrapper {

	@Override
	public String getLoggerName() {
		return null;
	}

	@Override
	public boolean isLoggable( Level level ) {
		return false;
	}

}
