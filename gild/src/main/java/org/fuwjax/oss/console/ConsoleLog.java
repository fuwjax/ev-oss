package org.fuwjax.oss.console;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface ConsoleLog {
	String value();

	String resource() default "src/test/resources";
	
	String target() default "target/console";
	
	boolean echo() default false;
}
