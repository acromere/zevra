package com.acromere.log.provider;

/**
 * Abstract base class for logging providers.
 * <p>
 * Subclasses must implement {@link #getLoggerWrapper(String)} to create
 * logger wrappers for the specific logging backend.
 */
public abstract class AbstractLoggingProvider implements LoggingProvider {

}
