package org.echovantage.sample;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.echovantage.metafactory.Meta;

@Retention(RUNTIME)
@Target(TYPE)
@Meta(SampleService.Factory.class)
public @interface SampleAnnotation {
	String value();
}
