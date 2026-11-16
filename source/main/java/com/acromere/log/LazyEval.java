package com.acromere.log;

/**
 * A functional interface for lazy evaluation of log arguments.
 * <p>
 * Use this when computing a value for a log message is expensive and should
 * only occur if the log statement will actually be emitted. The value is
 * computed by calling {@link #evaluate()} only when needed.
 * <p>
 * Example usage:
 * <pre>{@code
 * log.atDebug().log("Expensive result: %s", LazyEval.of(() -> expensiveComputation()));
 * }</pre>
 * <p>
 * This interface is designed to be used with method references and lambdas:
 * <pre>{@code
 * log.atDebug().log("Data: %s", LazyEval.of(data::expensiveToString));
 * }</pre>
 *
 * @param <T> the type of the value to be computed
 */
public interface LazyEval<T> {

	/**
	 * Helper method to explicitly wrap a {@code LazyEval} instance.
	 * Useful when type inference is needed.
	 *
	 * @param parameter the lazy evaluation to wrap
	 * @param <T> the type of the value
	 * @return the same {@code LazyEval} instance
	 */
	static <T> LazyEval<T> of( LazyEval<T> parameter ) {
		return parameter;
	}

	/**
	 * Computes and returns the value.
	 *
	 * @return the computed value
	 */
	T evaluate();

}
