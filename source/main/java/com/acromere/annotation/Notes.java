package com.acromere.annotation;

import java.lang.annotation.*;

/**
 * Container annotation for multiple {@link Note} annotations on the same element.
 *
 * <p>This annotation enables the repeatable use of {@code @Note} on a single
 * declaration. When multiple notes are applied, they are wrapped in this container
 * automatically by the compiler.</p>
 *
 * <p><strong>Keep in sync:</strong> the {@code @Target} of this container must
 * include every {@code ElementType} in {@link Note}'s {@code @Target}. If they
 * diverge, the compiler rejects repeated {@code @Note} usages on any element
 * type that this container does not target.</p>
 *
 * @see Note
 */
@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.MODULE, ElementType.RECORD_COMPONENT, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE } )
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
