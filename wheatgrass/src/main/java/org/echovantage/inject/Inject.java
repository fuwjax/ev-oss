package org.echovantage.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker annotation for injectable fields, methods, and constructors.
 *
 * @author fuwjax
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD,CONSTRUCTOR})
public @interface Inject {
}
