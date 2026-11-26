/**
 * Provider abstraction layer for the Zevra logging system.
 * <p>
 * This package defines the interfaces and abstract classes that decouple the
 * Zevra logging API from specific logging implementations. The main types are:
 * <ul>
 *   <li>{@link com.acromere.log.provider.LoggingProvider} - Factory for creating logger wrappers</li>
 *   <li>{@link com.acromere.log.provider.LoggerWrapper} - Interface for a logger backed by a specific implementation</li>
 *   <li>{@link com.acromere.log.provider.AbstractLoggingProvider} - Base class for logging provider implementations</li>
 *   <li>{@link com.acromere.log.provider.AbstractLoggerWrapper} - Base class for logger wrapper implementations</li>
 * </ul>
 * <p>
 * To integrate a new logging backend (e.g., SLF4J, Log4j), implement these interfaces
 * and configure the Zevra logging system to use your provider via
 * {@link com.acromere.log.Logging#setLoggingProvider(LoggingProvider)}.
 */
package com.acromere.log.provider;