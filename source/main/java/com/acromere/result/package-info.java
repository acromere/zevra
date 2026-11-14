/**
 * Optional container for values that may or may not be present.
 *
 * <p>This package provides the {@link com.acromere.result.Result} class,
 * a value-based container similar to {@code java.util.Optional} but with
 * support for failure states via exception tracking. Result objects are
 * immutable and thread-safe.</p>
 *
 * <p>Typical usage includes representing the outcome of operations that
 * may succeed or fail, allowing callers to chain operations without
 * explicit null checks or try-catch blocks.</p>
 *
 * @see com.acromere.result.Result
 */
package com.acromere.result;