package com.acromere.annotation;

import java.lang.annotation.*;

/**
 * Container annotation for multiple {@link Note} annotations on the same element.
 *
 * <p>This annotation enables the repeatable use of {@code @Note} on a single
 * declaration. When multiple notes are applied, they are wrapped in this container
 * automatically by the compiler.</p>
 *
 * @see Note
 */
@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.CONSTRUCTOR } )
@Retention( RetentionPolicy.SOURCE )
@Documented
@Inherited
public @interface Notes {

	/**
	 * The collection of {@link Note} annotations contained by this wrapper.
	 *
	 * @return array of notes
	 */
	Note[] value();

}
