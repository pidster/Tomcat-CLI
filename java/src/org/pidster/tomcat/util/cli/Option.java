/**
 * 
 */
package org.pidster.tomcat.util.cli;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pidster
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Option {

    String name();

    char single();

    String extended() default "";

    String value() default "";

    String description();

    boolean required() default false;

    boolean setter() default false;

}
