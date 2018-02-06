package org.homunculusframework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for annotating unfinished classes
 *
 * Created by aerlemann on 06.02.18.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR,
        ElementType.ANNOTATION_TYPE, ElementType.PACKAGE})
@Inherited
public @interface Unfinished {
    String value() default "This is unfinished. Use this only at your own risk.";
}
