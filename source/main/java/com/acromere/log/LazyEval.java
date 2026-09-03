package com.acromere.log;

public interface LazyEval<T> {

	static <T> LazyEval<T> of( LazyEval<T> parameter ) {
		return parameter;
	}

	T evaluate();

}
