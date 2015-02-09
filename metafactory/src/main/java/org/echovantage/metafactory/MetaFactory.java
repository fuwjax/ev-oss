/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.echovantage.metafactory;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates that this class name should be listed into the <tt>META-INF/services/CONTRACTNAME</tt>.
 * <p>
 * If the class for which this annotation is placaed only have one base class or one interface, then the CONTRACTNAME is
 * the fully qualified name of that type.
 * <p>
 * Otherwise, the {@link #value()} element is required to specify the contract type name.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(CLASS)
@Documented
@Target(ANNOTATION_TYPE)
public @interface MetaFactory {
    Class<?> value() default void.class;
}
