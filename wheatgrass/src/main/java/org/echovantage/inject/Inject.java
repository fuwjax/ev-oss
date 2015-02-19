package org.echovantage.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by fuwjax on 2/15/15.
 */
@Retention(RUNTIME)
@Target({CONSTRUCTOR, FIELD, METHOD})
public @interface Inject {
    //mimics JSR-330
}
