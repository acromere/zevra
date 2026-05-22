package com.acromere.annotation;

import java.lang.annotation.*;

@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.MODULE, ElementType.RECORD_COMPONENT, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE } )
@Retention( RetentionPolicy.SOURCE )
@Repeatable( Notes.class )
@Documented
@Inherited
public @interface Note {

	String value();

}
