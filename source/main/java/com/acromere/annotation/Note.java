package com.acromere.annotation;

import java.lang.annotation.*;

@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.MODULE, ElementType.RECORD_COMPONENT, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE } )
@Retention( RetentionPolicy.SOURCE )
@Repeatable( Notes.class )
@Documented
@Inherited
public @interface Note {

	/**
	 * Indicates that any thread may safely access this method and assumptions
	 * about thread-safe execution will be managed by the method implementation.
	 */
	String THREAD_SAFE = "any-thread";

	String value();

}
