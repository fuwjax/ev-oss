package org.echovantage.sample;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.echovantage.metafactory.MetaFactory;

@Retention(RUNTIME)
@Target(TYPE)
@MetaFactory(SampleService.Factory.class)
public @interface SampleAnnotation {
	String value();
}
