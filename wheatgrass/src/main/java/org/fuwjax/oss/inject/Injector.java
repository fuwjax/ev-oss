/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
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
package org.fuwjax.oss.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Named;

import org.fuwjax.oss.generic.TypeLiteral;
import org.fuwjax.oss.util.Annotations;

/**
 * Created by fuwjax on 2/20/15.
 */
public interface Injector {
	public static Injector newInjector(final Object... modules) throws ReflectiveOperationException{
		return newInjector(InjectionLibrary.DEFAULT, modules);
	}
	
    public static Injector newInjector(InjectionLibrary library, final Object... modules) throws ReflectiveOperationException {
        BindingStrategy[] bindings = new BindingStrategy[modules.length];
        InjectorImpl injector = new InjectorImpl(new ChainStrategy(bindings), library);
        for (int i = 0; i < modules.length; i++) {
            Object module = modules[i] instanceof Type ? injector.get((Type) modules[i]) : modules[i];
            bindings[i] = module instanceof BindingStrategy ? (BindingStrategy) module : new ReflectStrategy(module, library);
        }
        return injector;
    }

    public static Named named(String name) {
        return Annotations.of(Named.class, name);
    }

    <T> T inject(final T object) throws ReflectiveOperationException;

    <T> T inject(TypeLiteral<T> type, final T object) throws ReflectiveOperationException;

     <T> T get(final Class<T> type, Annotation... annotations) throws ReflectiveOperationException;
     
     <T> T get(final TypeLiteral<T> type, Annotation... annotations) throws ReflectiveOperationException;
     
     Injector scope();
}
