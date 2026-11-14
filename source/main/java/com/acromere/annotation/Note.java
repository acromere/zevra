package com.acromere.annotation;

import java.lang.annotation.*;

/**
 * A source-level annotation for attaching notes or hints to code elements.
 *
 * <p>This annotation is repeatable via the {@link Notes} container annotation and
 * is retained only at source level, making it useful for static analysis tools
 * and documentation purposes. The annotation supports a variety of predefined
 * thread-safety and usage constants.</p>
 *
 * @see Notes
 */
@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.MODULE, ElementType.RECORD_COMPONENT, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE } )
@Retention( RetentionPolicy.SOURCE )
@Repeatable( Notes.class )
@Documented
@Inherited
public @interface Note {

	/**
	 * This method is intended to be called only by the FX thread.
	 */
	String FX_THREAD = "fx-thread";

	/**
	 * This method is intended to be called only by a task thread.
	 */
	String TASK_THREAD = "task-thread";

	/**
	 * Indicates that any thread may safely access this method and assumptions
	 * about thread-safe execution will be managed by the method implementation.
	 */
	String ANY_THREAD = "any-thread";

	/**
	 * This field or method is intended to be used only for testing purposes.
	 */
	String TESTING_ONLY = "testing-only";

	/**
	 * The note text. Use one of the predefined constants for thread-safety
	 * annotations, or provide custom text for other purposes.
	 *
	 * @return the note text
	 */
	String value();

}
