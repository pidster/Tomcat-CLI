/**
 * 
 */
package org.apache.tomcat.util.cli;

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

    char trigger();

    String extended() default "";

    String description();

    boolean required() default false;

    boolean setter() default false;

}
