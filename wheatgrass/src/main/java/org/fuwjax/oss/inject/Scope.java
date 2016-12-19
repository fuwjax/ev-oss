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

import static org.fuwjax.oss.util.function.Functions.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.fuwjax.oss.generic.GenericMember;
import org.fuwjax.oss.generic.TypeLiteral;
import org.fuwjax.oss.util.Arrays2;
import org.fuwjax.oss.util.Iterables;
import org.fuwjax.oss.util.RunWrapException;

/**
 * Created by fuwjax on 2/17/15.
 */
class Scope implements Injector, BindingStrategy {
	private final Map<BindConstraint, Object> objects = new HashMap<>();
	private final BindingStrategy bindings;
	private final InjectionLibrary library;

	Scope(final BindingStrategy bindings, InjectionLibrary library) {
		this.bindings = bindings;
		this.library = library;
		objects.put(new BindConstraint(Injector.class), this);
	}

	private Object get(final BindConstraint constraint) throws ReflectiveOperationException {
		Object object = objects.get(constraint);
		if (object == null) {
			object = binding(constraint).get(this);
			objects.put(constraint, object);
		}
		return object;
	}

	
	private void inject(final BindConstraint constraint, final Object target) throws ReflectiveOperationException {
		assert constraint != null;
		assert target != null;
		objects.put(constraint, target);
		inject(library.injection(constraint.type()), target);
	}

	public Object invoke(final Object target, final GenericMember member) throws ReflectiveOperationException {
		try {
			final Object[] args = Arrays2.transform(member.paramTypes(), new Object[0], function(t -> get(new BindConstraint(t))));
			return member.invoke(target, args);
		} catch (final RunWrapException e) {
			throw e.throwIf(ReflectiveOperationException.class, x -> new ReflectiveOperationException("Could not invoke "+member.source(),e));
		}
	}

	private Binding binding(final BindConstraint constraint) throws ReflectiveOperationException {
		final Binding binding = bindings.bindingFor(constraint);
		if (binding == null) {
			return constraint.defaultBinding();
		}
		return binding;
	}

	public Object create(final BindConstraint constraint) throws ReflectiveOperationException {
		final Injection spec = library.injection(constraint.type());
		final Object o = invoke(null, spec.constructor());
		objects.put(constraint, o);
		inject(spec, o);
		return o;
	}

	private void inject(final Injection spec, final Object object) throws ReflectiveOperationException {
		for (final GenericMember member : Iterables.over(spec.members())) {
			invoke(object, member);
		}
	}

    public <T> T inject(final T object) throws ReflectiveOperationException {
        inject(new BindConstraint(object.getClass()), object);
        return object;
    }

    public <T> T inject(TypeLiteral<T> type, final T object) throws ReflectiveOperationException {
        assert object.getClass().equals(type.getRawType());
        inject(new BindConstraint(type), object);
        return object;
    }

    public <T> T get(final Class<T> type, Annotation... annotations) throws ReflectiveOperationException {
        return (T) get(new BindConstraint(type, annotations));
    }

    public <T> T get(final TypeLiteral<T> type, Annotation... annotations) throws ReflectiveOperationException {
        return (T) get(new BindConstraint(type, annotations));
    }
    
    public Object get(Type type, Annotation...annotations) throws ReflectiveOperationException{
    	return get(new BindConstraint(type, annotations));
    }

    public Scope scope(){
    	return this;
    }

	@Override
	public Binding bindingFor(BindConstraint constraint) {
		return bindings.bindingFor(constraint);
	}
}
