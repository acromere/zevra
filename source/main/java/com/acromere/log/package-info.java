/**
 * A fluent, provider-independent logging system.
 * <p>
 * The logging API provides a fluent logging facade patterned after Google Fluent Logger,
 * designed to be lightweight, modular, and compatible with Java modules.
 * <p>
 * Core classes include:
 * <ul>
 *   <li>{@link com.acromere.log.Logging Logging} - Factory for creating {@link com.acromere.log.Logger Logger} instances and managing the active {@link com.acromere.log.provider.LoggingProvider LoggingProvider}</li>
 *   <li>{@link com.acromere.log.Logger Logger} - Primary logger providing fluent logging entry points</li>
 *   <li>{@link com.acromere.log.LogApi LogApi} - Fluent logging interface supporting formatted messages, lazy evaluation, contextual metadata, and causes</li>
 *   <li>{@link com.acromere.log.Log Log} - Utility methods for configuring JVM logging handlers, formats, and log levels</li>
 *   <li>{@link com.acromere.log.LogLevel LogLevel} - Standard logging levels (ERROR, WARN, INFO, DEBUG, TRACE)</li>
 *   <li>{@link com.acromere.log.LazyEval LazyEval} - Support for lazy evaluation of log arguments</li>
 * </ul>
 */
package com.acromere.log;
