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
package org.fuwjax.oss.generic;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/18/15.
 * @param <T> the proxied parameterized type
 */
public abstract class TypeLiteral<T> implements ParameterizedType {
	private final ParameterizedType type;

	protected TypeLiteral() {
		assert getClass().getGenericSuperclass() instanceof ParameterizedType: "TypeTemplate instances should be anonymous classes";
		final ParameterizedType t = (ParameterizedType) getClass().getGenericSuperclass();
		assert TypeLiteral.class.equals(t.getRawType()): "TypeTemplate instances must be direct anonymous classes";
		final Type arg = t.getActualTypeArguments()[0];
		assert arg instanceof ParameterizedType: "TypeTemplate anonymous instances should only be used for generic, non-array types";
		type = (ParameterizedType) arg;
	}

	public AnnotatedDeclaration with(final Annotation... annotations) {
		return new AnnotatedDeclaration(this, annotations);
	}

	@Override
	public String getTypeName() {
		return type.getTypeName();
	}

	@Override
	public Type[] getActualTypeArguments() {
		return type.getActualTypeArguments();
	}

	@Override
	public Type getOwnerType() {
		return type.getOwnerType();
	}

	@Override
	public Type getRawType() {
		return type.getRawType();
	}

	@Override
	public String toString() {
		return type.toString();
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return type.equals(obj);
	}
}
