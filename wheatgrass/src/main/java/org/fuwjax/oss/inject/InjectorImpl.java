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

import org.fuwjax.oss.generic.TypeLiteral;

class InjectorImpl implements Injector, BindingStrategy {
    private final BindingStrategy bindings;
	private InjectionLibrary library;

    protected InjectorImpl(final BindingStrategy bindings, InjectionLibrary library) {
        this.bindings = bindings;
		this.library = library;
    }

    public Scope scope() {
        return new Scope(bindings, library);
    }

    public <T> T inject(final T object) throws ReflectiveOperationException {
        return scope().inject(object);
    }

    public <T> T inject(TypeLiteral<T> type, final T object) throws ReflectiveOperationException {
        return scope().inject(type, object);
    }

    public <T> T get(final Class<T> type, Annotation... annotations) throws ReflectiveOperationException {
        return scope().get(type, annotations);
    }

    public <T> T get(final TypeLiteral<T> type, Annotation... annotations) throws ReflectiveOperationException {
        return scope().get(type, annotations);
    }

	Object get(Type type, Annotation...annotations) throws ReflectiveOperationException {
		return scope().get(type, annotations);
	}

	@Override
	public Binding bindingFor(BindConstraint constraint) {
		return bindings.bindingFor(constraint);
	}
	
}
