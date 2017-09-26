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
package org.fuwjax.oss.util;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Types {
	public static final Type[] NO_PARAMS = new Type[0];
	private static Map<Class<?>, Class<?>> parent = new HashMap<>();
	private static Map<Class<?>, Class<?>> box = new HashMap<>();
	private static Map<Class<?>, List<Type>> supers = new HashMap<>();

	static {
		parent.put(byte.class, short.class);
		parent.put(short.class, int.class);
		parent.put(char.class, int.class);
		parent.put(int.class, long.class);
		parent.put(long.class, float.class);
		parent.put(float.class, double.class);
		box(boolean.class, Boolean.class);
		box(byte.class, Byte.class);
		box(short.class, Short.class);
		box(char.class, Character.class);
		box(int.class, Integer.class);
		box(long.class, Long.class);
		box(float.class, Float.class);
		box(double.class, Double.class);

		supers.put(null, Collections.emptyList());
		supers.put(Object[].class, Arrays.asList(Object.class, Serializable.class, Cloneable.class));
		supers.put(boolean.class, Arrays.asList(Boolean.class));
		supers.put(byte.class, Arrays.asList(short.class, Byte.class));
		supers.put(short.class, Arrays.asList(int.class, Short.class));
		supers.put(char.class, Arrays.asList(int.class, Character.class));
		supers.put(int.class, Arrays.asList(long.class, Integer.class));
		supers.put(long.class, Arrays.asList(float.class, Long.class));
		supers.put(float.class, Arrays.asList(double.class, Float.class));
		supers.put(double.class, Arrays.asList(Double.class));
		supers.put(Boolean.class, with(classSupers(Boolean.class), boolean.class));
		supers.put(Byte.class, with(classSupers(Byte.class), byte.class));
		supers.put(Short.class, with(classSupers(Short.class), short.class));
		supers.put(Character.class, with(classSupers(Character.class), char.class));
		supers.put(Integer.class, with(classSupers(Integer.class), int.class));
		supers.put(Long.class, with(classSupers(Long.class), long.class));
		supers.put(Float.class, with(classSupers(Float.class), float.class));
		supers.put(Double.class, with(classSupers(Double.class), double.class));
	}

	private static List<Type> classSupers(Class<?> c) {
		return with(Arrays.asList(c.getGenericInterfaces()), c.getGenericSuperclass());
	}

	private static <T> List<T> with(List<T> list, T t) {
		try {
			list.add(t);
			return list;
		} catch (UnsupportedOperationException e) {
			List<T> l = new ArrayList<>(list);
			l.add(t);
			return l;
		}
	}

	public static final Comparator<Type> TYPE_COMPARATOR = (t1, t2) -> {
		if (Objects.equals(t1, t2)) {
			return 0;
		}
		if (Types.isAssignable(t1, t2, true)) {
			return 1;
		}
		if (Types.isAssignable(t2, t1, true)) {
			return -1;
		}
		return t1.getTypeName().compareTo(t2.getTypeName());
	};

	private static void box(Class<?> primitive, Class<?> boxed) {
		box.put(primitive, boxed);
		box.put(boxed, primitive);
	}

	private static Type superType(Class<?> c) {
		if (c.isArray() || (c.isInterface() && c.getInterfaces().length == 0)) {
			return Object.class;
		}
		return c.isPrimitive() ? parent.get(c) : c.getGenericSuperclass();
	}

	public static boolean isAssignable(Type lhs, Type rhs) {
		return (rhs == null || lhs != null) && (isAssignable(componentType(lhs), componentType(rhs), true)
				|| isAssignable(lhs, rhs, true) || isAssignable(lhs, box.get(rhs), true));
	}

	private static boolean isAssignable(Type lhs, Type rhs, boolean allowUnchecked) {
		if (rhs == null) {
			return false;
		}
		if (lhs.equals(rhs)) {
			return true;
		}
		if (rhs instanceof Class) {
			Class<?> right = c(rhs);
			if (allowUnchecked && lhs instanceof ParameterizedType) {
				if (p(lhs).getRawType().equals(rhs)) {
					return true;
				}
			}
			if (isAssignable(lhs, superType(right), allowUnchecked)) {
				return true;
			}
			for (Type iface : right.getGenericInterfaces()) {
				if (isAssignable(lhs, iface, allowUnchecked)) {
					return true;
				}
			}
		} else if (rhs instanceof ParameterizedType) {
			ParameterizedType right = p(rhs);
			if (isAssignable(lhs, right.getRawType(), false)) {
				return true;
			}
			Class<?> self = c(right.getRawType());
			if (isAssignable(lhs, subst(superType(self), right), allowUnchecked)) {
				return true;
			}
			for (Type iface : self.getGenericInterfaces()) {
				if (isAssignable(lhs, subst(iface, right), allowUnchecked)) {
					return true;
				}
			}
			if (isAssignable(lhs, capture(right), allowUnchecked)) {
				return true;
			}
			if (lhs instanceof ParameterizedType) {
				ParameterizedType left = p(lhs);
				if (left.getRawType().equals(right.getRawType())
						&& contains(left.getActualTypeArguments(), right.getActualTypeArguments())) {
					return true;
				}
			}
		} else if (rhs instanceof GenericArrayType) {
			if (isAssignable(lhs, Object.class, allowUnchecked)) {
				return true;
			}
			for (Type iface : Object[].class.getGenericInterfaces()) {
				if (isAssignable(lhs, iface, allowUnchecked)) {
					return true;
				}
			}
		} else if (rhs instanceof TypeVariable) {
			TypeVariable right = v(rhs);
			for (Type bound : right.getBounds()) {
				if (isAssignable(lhs, bound, allowUnchecked)) {
					return true;
				}
			}
		}
		return false;
	}

	private static Type capture(Type t) {
		return null;
	}

	private static boolean contains(Type[] lhs, Type[] rhs) {
		for (int i = 0; i < lhs.length; i++) {
			if (!contains(lhs[i], rhs[i])) {
				return false;
			}
		}
		return true;
	}

	private static boolean contains(Type lhs, Type rhs) {
		for (Type left : upperBounds(lhs)) {
			for (Type right : upperBounds(rhs)) {
				if (!isAssignable(left, right)) {
					return false;
				}
			}
		}
		for (Type left : lowerBounds(lhs)) {
			for (Type right : lowerBounds(rhs)) {
				if (!isAssignable(right, left)) {
					return false;
				}
			}
		}
		return true;
	}

	private static Type[] lowerBounds(Type t) {
		if (t instanceof WildcardType) {
			return w(t).getLowerBounds();
		}
		if (t instanceof TypeVariable) {
			return NO_PARAMS;
		}
		return new Type[] { t };
	}

	private static Type[] upperBounds(Type t) {
		if (t instanceof WildcardType) {
			return w(t).getUpperBounds();
		}
		if (t instanceof TypeVariable) {
			return v(t).getBounds();
		}
		return new Type[] { t };
	}

	public static Type subst(Type t, ParameterizedType mapping) {
		Type result;
		if (t == null) {
			return null;
		} else if (t instanceof TypeVariable) {
			TypeVariable v = v(t);
			Class<?> raw = c(mapping.getRawType());
			int index = Arrays.asList(raw.getTypeParameters()).indexOf(v);
			if (index == -1) {
				if (mapping.getOwnerType() instanceof ParameterizedType) {
					return subst(t, p(mapping.getOwnerType()));
				}
				throw new IllegalArgumentException("Variable " + t + " is not present in " + mapping);
			}
			Type arg = mapping.getActualTypeArguments()[index];
			result = subst(arg == null ? wildcardOf(v.getBounds(), NO_PARAMS) : arg, mapping);
		} else if (t instanceof GenericArrayType) {
			final GenericArrayType array = a(t);
			Type comp = subst(array.getGenericComponentType(), mapping);
			result = arrayOf(comp);
		} else if (t instanceof ParameterizedType) {
			ParameterizedType p = p(t);
			Type owner = subst(p.getOwnerType(), mapping);
			Type[] args = subst(p.getActualTypeArguments(), mapping);
			result = paramOf(owner, p.getRawType(), args);
		} else if (t instanceof WildcardType) {
			WildcardType w = w(t);
			Type[] upper = subst(w.getUpperBounds(), mapping);
			Type[] lower = subst(w.getLowerBounds(), mapping);
			result = wildcardOf(upper, lower);
		} else if (t instanceof Class) {
			result = t;
		} else {
			throw new IllegalArgumentException("Unknown type " + t);
		}
		return result.equals(t) ? t : result;
	}

	public static Type[] subst(Type[] types, ParameterizedType mapping) {
		return Arrays2.transform(types, NO_PARAMS, t -> subst(t, mapping));
	}

	private static ParameterizedType paramOf(Type owner, Type raw, Type[] args) {
		return new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return args;
			}

			@Override
			public Type getRawType() {
				return raw;
			}

			@Override
			public Type getOwnerType() {
				return owner;
			}

			public boolean equals(Object obj) {
				if (obj instanceof ParameterizedType) {
					ParameterizedType o = p((Type) obj);
					return Objects.equals(owner, o.getOwnerType()) && Objects.equals(raw, o.getRawType())
							&& Arrays.equals(args, o.getActualTypeArguments());
				}
				return false;
			}

			public int hashCode() {
				return Arrays.hashCode(args) ^ Objects.hashCode(owner) ^ Objects.hashCode(raw);
			}
		};
	}

	private static WildcardType wildcardOf(Type[] upper, Type[] lower) {
		return new WildcardType() {
			@Override
			public Type[] getUpperBounds() {
				return upper;
			}

			@Override
			public Type[] getLowerBounds() {
				return lower;
			}

			public boolean equals(Object obj) {
				if (obj instanceof WildcardType) {
					WildcardType o = w((Type) obj);
					return Arrays.equals(upper, o.getUpperBounds()) && Arrays.equals(lower, o.getLowerBounds());
				}
				return false;
			}

			public int hashCode() {
				return Arrays.hashCode(lower) ^ Arrays.hashCode(upper);
			}
		};
	}

	private static GenericArrayType arrayOf(Type comp) {
		return new GenericArrayType() {
			@Override
			public Type getGenericComponentType() {
				return comp;
			}

			@Override
			public boolean equals(Object obj) {
				return obj instanceof GenericArrayType && Objects.equals(comp, a((Type) obj).getGenericComponentType());
			}

			@Override
			public int hashCode() {
				return Objects.hashCode(comp);
			}
		};
	}

	private static Type componentType(Type t) {
		if (t instanceof GenericArrayType) {
			return a(t).getGenericComponentType();
		}
		return t instanceof Class ? c(t).getComponentType() : null;
	}

	private static ParameterizedType p(Type t) {
		return (ParameterizedType) t;
	}

	private static Class<?> c(Type t) {
		return (Class<?>) t;
	}

	private static TypeVariable v(Type t) {
		return (TypeVariable) t;
	}

	private static WildcardType w(Type t) {
		return (WildcardType) t;
	}

	private static GenericArrayType a(Type t) {
		return (GenericArrayType) t;
	}

	public static boolean isInstantiable(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType p = p(type);
			for (Type t : p.getActualTypeArguments()) {
				if (!isInstantiable(t)) {
					return false;
				}
			}
			return true;
		}
		return type instanceof Class;
	}

	public static boolean isVoid(Type type) {
		return void.class.equals(type) || Void.class.equals(type);
	}

	public static AnnotatedElement annotatedElement(Annotation... annotations) {
		Map<Class<?>, Annotation> map = Arrays.asList(annotations).stream()
				.collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
		return new AnnotatedElement() {
			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return annotationClass.cast(map.get(annotationClass));
			}

			@Override
			public Annotation[] getAnnotations() {
				return annotations;
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return getAnnotations();
			}
		};
	}

	public static List<Type> directSupers(Type type) {
		if (supers.containsKey(type)) {
			return supers.get(type);
		}
		if (type instanceof GenericArrayType) {
			GenericArrayType a = (GenericArrayType) type;
			return directSupers(a.getGenericComponentType()).stream().map(Types::arrayOf).collect(Collectors.toList());
		}
		if (type instanceof Class) {
			Class<?> c = (Class<?>) type;
			if (c.isArray()) {
				return directSupers(c.getComponentType()).stream().map(Types::arrayOf).collect(Collectors.toList());
			}
			return classSupers(c);
		}
		if (type instanceof ParameterizedType) {
			final ParameterizedType p = (ParameterizedType) type;
			return classSupers((Class<?>) p.getRawType()).stream().map(s -> subst(s, p)).collect(Collectors.toList());
		}
		throw new UnsupportedOperationException("Unknown type " + type.getClass());
	}

	public static Set<Type> supers(Type type) {
		Set<Type> types = new HashSet<>();
		List<Type> check = new ArrayList<>(Arrays.asList(type));
		while (!check.isEmpty()) {
			Type t = check.remove(0);
			if (types.add(t)) {
				check.addAll(directSupers(type));
			}
		}
		return types;
	}

	public static boolean containsDep(Type decl, Type val) {
		if (decl instanceof ParameterizedType) {
			ParameterizedType p = (ParameterizedType) decl;
			if (val instanceof ParameterizedType) {
				ParameterizedType v = (ParameterizedType) val;
				if (!p.getRawType().equals(v.getRawType())) {
					return false;
				}
				for (int i = 0; i < p.getActualTypeArguments().length; i++) {
					if (!contains(p.getActualTypeArguments()[i], v.getActualTypeArguments()[i])) {
						return false;
					}
				}
				return true;
			}
			return p.getRawType().equals(val);
		}
		if (decl instanceof Class) {
			return decl.equals(val);
		}
		if (decl instanceof GenericArrayType) {
			GenericArrayType a = (GenericArrayType) decl;
			if (val instanceof GenericArrayType) {
				GenericArrayType v = ((GenericArrayType) val);
				return contains(a.getGenericComponentType(), v.getGenericComponentType());
			}
			if (val instanceof Class) {
				Class<?> v = (Class<?>) val;
				return contains(a.getGenericComponentType(), v.getComponentType());
			}
			return false;
		}
		if (decl instanceof WildcardType) {
			WildcardType w = (WildcardType) decl;
			if (val instanceof WildcardType) {

			}
		}
		return false;
	}
}
