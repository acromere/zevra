package com.acromere.log;

public interface LazyEval<T> {

	T evaluate();

	static <T> LazyEval<T> of( LazyEval<T> parameter ) {
		return parameter;
	}

}
