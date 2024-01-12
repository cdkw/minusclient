package org.apache.logging.log4j.core.config.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Plugin {
   String EMPTY = "";

   String name();

   String category();

   String elementType() default "";

   boolean printObject() default false;

   boolean deferChildren() default false;
}
