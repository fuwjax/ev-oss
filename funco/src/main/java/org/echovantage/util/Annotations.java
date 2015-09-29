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
package org.echovantage.util;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.summingInt;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.echovantage.util.collection.ReflectList;

/**
 * Created by fuwjax on 2/26/15.
 */
public class Annotations {
	public static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	public static <A extends Annotation> A of(final Class<A> annotationType, final Map<String, ?> values) {
		return proxy(annotationType, new AnnotationHandler(annotationType, values));
	}

	public static <A extends Annotation> A of(final Class<A> annotationType, final Object value) {
		return of(annotationType, singletonMap("value", value));
	}

	public static <A extends Annotation> A of(final Class<A> annotationType) {
		return of(annotationType, emptyMap());
	}

	private static final class AnnotationHandler implements InvocationHandler, Annotation {
		private static final Method cloneMethod;

		static {
			try {
				cloneMethod = Object.class.getDeclaredMethod("clone");
				cloneMethod.setAccessible(true);
			} catch (final ReflectiveOperationException e) {
				throw new RunWrapException(e);
			}
		}

		private final Class<? extends Annotation> type;
		private final List<Method> methods;
		private final Map<String, Object> values = new HashMap<>();

		public AnnotationHandler(final Class<? extends Annotation> type, final Map<String, ?> values) {
			this.type = type;
			methods = Arrays.asList(type.getDeclaredMethods());
			for (final Method m : methods) {
				Object o = values.get(m.getName());
				if (o == null) {
					o = m.getDefaultValue();
				}
				if (o == null) {
					throw new IncompleteAnnotationException(type, m.getName());
				}
				this.values.put(m.getName(), o);
			}
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (type.equals(method.getDeclaringClass())) {
				return copyOf(values.get(method.getName()));
			}
			return method.invoke(this, args);
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return type;
		}

		private static Object copyOf(final Object value) throws InvocationTargetException, IllegalAccessException {
			if (!value.getClass().isArray() || Array.getLength(value) == 0) {
				return value;
			}
			return cloneMethod.invoke(value);
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append('@').append(type.getName()).append('(')
					.append(values.entrySet().stream().map(e -> e.getKey() + '=' + valueToString(e.getValue())).collect(Collectors.joining(", ")));
			return builder.append(')').toString();
		}

		private String valueToString(final Object value) {
			if (!value.getClass().isArray()) {
				return value.toString();
			}
			return ReflectList.asList(value).toString();
		}

		@Override
		public boolean equals(final Object obj) {
			if (!type.isInstance(obj)) {
				return false;
			}
			try {
				for (final Method m : methods) {
					final Object o = m.invoke(obj);
					if (!Objects.deepEquals(o, values.get(m.getName()))) {
						return false;
					}
				}
				return true;
			} catch (final ReflectiveOperationException e) {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return values.entrySet().stream().collect(summingInt(e -> 127 * e.getKey().hashCode() ^ valueHash(e.getValue())));
		}

		private static int valueHash(final Object value) {
			return Arrays.deepHashCode(new Object[] { value }) - 31;
		}
	}
}
