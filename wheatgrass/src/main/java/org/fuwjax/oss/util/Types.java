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
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Types {
    public static final Type[] NO_PARAMS = new Type[0];
    private static Map<Class<?>, Class<?>> parent = new HashMap<>();
    private static Map<Class<?>, Class<?>> box = new HashMap<>();

    static{
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
    }

    public static final Comparator<Type> TYPE_COMPARATOR = (t1, t2) -> {
        if(Objects.equals(t1, t2)){
            return 0;
        }
        if (Types.isAssignable(t1, t2, true)){
            return 1;
        }
        if (Types.isAssignable(t2, t1, true)){
            return -1;
        }
        return t1.getTypeName().compareTo(t2.getTypeName());
    };

    private static void box(Class<?> primitive, Class<?> boxed) {
        box.put(primitive, boxed);
        box.put(boxed, primitive);
    }

    private static Type superType(Class<?> c){
        if(c.isArray() || (c.isInterface() && c.getInterfaces().length == 0)){
            return Object.class;
        }
        return c.isPrimitive() ? parent.get(c) : c.getGenericSuperclass();
    }

    public static boolean isAssignable(Type decl, Type val) {
        return (val == null || decl != null) && ((!isPrimitive(componentType(decl)) && isAssignable(componentType(decl), componentType(val), true)) || isAssignable(decl, val, true) || isAssignable(decl, box.get(val), true));
    }

    private static boolean isAssignable(Type decl, Type val, boolean allowUnchecked){
        if(val == null){
            return false;
        }
        if(val.equals(decl)){
            return true;
        }
        if(val instanceof Class) {
            Class<?> right = c(val);
            if (allowUnchecked && decl instanceof ParameterizedType) {
                if (p(decl).getRawType().equals(val)) {
                    return true;
                }
            }
            if (isAssignable(decl, superType(right), allowUnchecked)) {
                return true;
            }
            for(Type iface: right.getGenericInterfaces()){
                if(isAssignable(decl, iface, allowUnchecked)){
                    return true;
                }
            }
        }else if(val instanceof ParameterizedType){
            ParameterizedType right = p(val);
            if(isAssignable(decl, right.getRawType(), false)){
                return true;
            }
            Class<?> self = c(right.getRawType());
            if(isAssignable(decl, subst(superType(self), right), allowUnchecked)){
                return true;
            }
            for(Type iface: self.getGenericInterfaces()){
                if(isAssignable(decl, subst(iface, right), allowUnchecked)){
                    return true;
                }
            }
            if(isAssignable(decl, capture(right), allowUnchecked)){
                return true;
            }
            if(decl instanceof ParameterizedType){
                ParameterizedType left = p(decl);
                if(left.getRawType().equals(right.getRawType()) && contains(left.getActualTypeArguments(), right.getActualTypeArguments())){
                    return true;
                }
            }
        }else if(val instanceof GenericArrayType){
            if (isAssignable(decl, Object.class, allowUnchecked)) {
                return true;
            }
            for(Type iface: Object[].class.getGenericInterfaces()){
                if(isAssignable(decl, iface, allowUnchecked)){
                    return true;
                }
            }
        }else if(val instanceof TypeVariable){
            TypeVariable<?> right = v(val);
            for(Type bound: right.getBounds()){
                if(isAssignable(bound, decl, allowUnchecked)){
                    return true;
                }
            }
        }
        return false;
    }

    private static Type capture(Type t){
        return null;
    }

    private static boolean contains(Type[] lhs, Type[] rhs){
        for(int i=0;i<lhs.length;i++){
            if(!contains(lhs[i], rhs[i])){
                return false;
            }
        }
        return true;
    }

    private static boolean contains(Type lhs, Type rhs) {
        for(Type left: upperBounds(lhs)){
            for(Type right: upperBounds(rhs)){
                if(!isAssignable(left, right)){
                    return false;
                }
            }
        }
        for(Type left: lowerBounds(lhs)){
            for(Type right: lowerBounds(rhs)){
                if(!isAssignable(right, left)){
                    return false;
                }
            }
        }
        return true;
    }

    private static Type[] lowerBounds(Type t){
        if(t instanceof WildcardType){
            return w(t).getLowerBounds();
        }
        if(t instanceof TypeVariable){
            return NO_PARAMS;
        }
        return new Type[]{t};
    }

    private static Type[] upperBounds(Type t){
        if(t instanceof WildcardType){
            return w(t).getUpperBounds();
        }
        if(t instanceof TypeVariable){
            return v(t).getBounds();
        }
        return new Type[]{t};
    }

    public static Type subst(Type decl, ParameterizedType mapping){
        Type result;
        if(decl == null){
            return null;
        }else if(decl instanceof TypeVariable){
            TypeVariable<?> v = v(decl);
            Type arg = resolve(mapping, v);
            result = arg == null ? v : arg; //subst(wildcardOf(v.getBounds(), NO_PARAMS),mapping)
        }else if(decl instanceof GenericArrayType){
            final GenericArrayType array = a(decl);
            Type comp = subst(array.getGenericComponentType(), mapping);
            result = arrayOf(comp);
        }else if(decl instanceof ParameterizedType){
            ParameterizedType p = p(decl);
            Type owner = subst(p.getOwnerType(), mapping);
            Type[] args = subst(p.getActualTypeArguments(), mapping);
            result = paramOf(owner, p.getRawType(), args);
        }else if(decl instanceof WildcardType){
            WildcardType w = w(decl);
            Type[] upper = subst(w.getUpperBounds(), mapping);
            Type[] lower = subst(w.getLowerBounds(), mapping);
            result = reduceBounds(upper, lower);
        }else if(decl instanceof Class){
            result = decl;
        }else{
            throw new IllegalArgumentException("Unknown type "+name(decl));
        }
        if(decl.equals(result)){
        	return decl;
        }
        System.out.println("subst "+ name(decl)+" with "+ name(mapping)+" returned "+name(result));
        return result;
    }
    
    private static Type reduceBounds(Type[] upper, Type[] lower) {
    	if(upper.length == 1 && upper[0] instanceof WildcardType){
    		return upper[0];
    	}
    	if(lower.length == 1 && lower[0] instanceof WildcardType){
    		return lower[0];
    	}
		return wildcardOf(upper, lower);
	}

	private static Type resolve(Type t, TypeVariable<?> v){
    	return t instanceof ParameterizedType ? resolve(p(t), v) : null;
    }

    private static Type resolve(ParameterizedType p, TypeVariable<?> v) {
    	int index = Arrays.asList(c(p.getRawType()).getTypeParameters()).indexOf(v);
        if(index != -1){
        	System.out.println("resolving "+name(v)+" against "+name(p) + " found "+name(p.getActualTypeArguments()[index]));
        	return p.getActualTypeArguments()[index];
        }
    	System.out.println("resolving "+name(v)+" against "+name(p) + " found nothing");
        return null;
//		Type result = resolve(p.getOwnerType(), v);
//    	if(result != null){
//    		return result;
//    	}
//    	result = resolve(subst(c(p.getRawType()).getGenericSuperclass(), p), v);
//    	if(result != null){
//    		return result;
//    	}
//    	result = Arrays.asList(c(p.getRawType()).getGenericInterfaces()).stream().map(i -> resolve(subst(i, p), v)).findFirst().orElse(null);
//        return result;
	}

	public static Type[] subst(Type[] decls, ParameterizedType mapping){
        return Arrays2.transform(decls, NO_PARAMS, t -> subst(t, mapping));
    }

    private static ParameterizedType paramOf(Type owner, Type raw, Type[] args){
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
            
            @Override
            public String toString() {
            	return name(this);
            }

            @Override
			public boolean equals(Object obj) {
                if(obj instanceof ParameterizedType) {
                    ParameterizedType o = p((Type)obj);
                    return Objects.equals(owner, o.getOwnerType()) && Objects.equals(raw, o.getRawType()) && Arrays.equals(args, o.getActualTypeArguments());
                }
                return false;
            }

            @Override
			public int hashCode() {
                return Arrays.hashCode(args) ^ Objects.hashCode(owner) ^ Objects.hashCode(raw);
            }
        };
    }

    private static WildcardType wildcardOf(Type[] upper, Type[] lower){
        return new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return upper;
            }

            @Override
            public Type[] getLowerBounds() {
                return lower;
            }
            
            @Override
            public String toString() {
            	return name(this);
            }

            @Override
			public boolean equals(Object obj) {
                if(obj instanceof WildcardType) {
                    WildcardType o = w((Type)obj);
                    return Arrays.equals(upper, o.getUpperBounds()) && Arrays.equals(lower, o.getLowerBounds());
                }
                return false;
            }

            @Override
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
            public String toString() {
            	return name(this);
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

    private static Type componentType(Type t){
        if(t instanceof GenericArrayType){
            return a(t).getGenericComponentType();
        }
        return t instanceof Class ? c(t).getComponentType() : null;
    }

    private static ParameterizedType p(Type t){
        return (ParameterizedType)t;
    }

    private static Class<?> c(Type t){
        return (Class<?>)t;
    }

    private static TypeVariable<?> v(Type t){
        return (TypeVariable<?>)t;
    }

    private static WildcardType w(Type t){
        return (WildcardType)t;
    }

    private static GenericArrayType a(Type t){
        return (GenericArrayType)t;
    }

    public static boolean isInstantiable(Type type) {
        if(type instanceof ParameterizedType){
            ParameterizedType p = p(type);
            for(Type t: p.getActualTypeArguments()){
                if(!isInstantiable(t)){
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

	public static Class<?> rawType(Type type) {
        if(type == null){
            return null;
        }
        if(type instanceof Class) {
            return c(type);
        }
        if(type instanceof ParameterizedType){
        	return c(p(type).getRawType());
        }
        if(type instanceof GenericArrayType){
        	return Array.newInstance(rawType(componentType(type)), 0).getClass();
        }
        if(type instanceof TypeVariable){
        	return rawType(v(type).getBounds()[0]);
        }
        if(type instanceof WildcardType){
        	return rawType(w(type).getUpperBounds()[0]);
        }
        assert false: "Unknown type "+type.getClass();
        return null;
	}
	
	public static String name(Type type){
		if(type == null){
			return null;
		}
		if(type instanceof Class){
			return c(type).getName();
		}
		if(type instanceof ParameterizedType){
			ParameterizedType p = p(type);
			return (p.getOwnerType() != null ? name(p.getOwnerType())+"." + c(p.getRawType()).getSimpleName() : name(p.getRawType()))+Arrays.asList(p.getActualTypeArguments()).stream().map(Types::name).collect(Collectors.joining(",","<",">"));
		}
		if(type instanceof TypeVariable){
			TypeVariable<?> v = v(type);
			if(v.getGenericDeclaration() instanceof Class){
				return ((Class<?>)v.getGenericDeclaration()).getName()+"."+v.getName();
			}
			if(v.getGenericDeclaration() instanceof Method){
				Method m = (Method)v.getGenericDeclaration();
				return m.getDeclaringClass().getName()+"."+m.getName()+"."+v.getName();
			}
			Constructor<?> c = (Constructor<?>)v.getGenericDeclaration();
			return c.getDeclaringClass().getName()+".new."+v.getName();
		}
		if(type instanceof GenericArrayType){
			return name(a(type).getGenericComponentType())+"[]";
		}
		if(type instanceof WildcardType){
			WildcardType w = w(type);
			if(w.getLowerBounds().length > 0){
				return "? super "+Arrays.asList(w.getLowerBounds()).stream().map(Types::name).collect(Collectors.joining(" & "));
			}else if(w.getUpperBounds().length == 1 && Object.class.equals(w.getUpperBounds()[0])){
				return "?";
			}
			return "? super "+Arrays.asList(w.getUpperBounds()).stream().map(Types::name).collect(Collectors.joining(" & "));
		}
		return "WTF!!"+type+"!!";
	}

	public static boolean isPrimitive(Type type) {
		return type instanceof Class && c(type).isPrimitive();
	}

	private static Map<Type, Type> primitiveParents = new HashMap<>();
	static{
		primitiveParents.put(byte.class, short.class);
		primitiveParents.put(char.class, int.class);
		primitiveParents.put(short.class, int.class);
		primitiveParents.put(int.class, long.class);
		primitiveParents.put(long.class, float.class);
		primitiveParents.put(float.class, double.class);
	}
	
	public static boolean widensTo(Type from, Type to) {
		Type test = primitiveParents.get(from);
		while(test != null && !Objects.equals(test, to)){
			test = primitiveParents.get(test);
		}
		return test != null;
	}
	
	public static boolean isArray(Type t){
		return t instanceof GenericArrayType || (t instanceof Class && ((Class<?>)t).isArray());
	}
	
	public static Type component(Type t){
		if(t instanceof GenericArrayType){
			return ((GenericArrayType)t).getGenericComponentType();
		}
		if(t instanceof Class){
			return ((Class<?>)t).getComponentType();
		}
		return null;
	}
	
	public static Stream<Type> supers(Type t){
		if(t instanceof Class){
			Class<?> c = (Class<?>)t;
			List<Type> types = supers.get(c);
			return Stream.concat(Stream.of(c, c.getGenericSuperclass()), Stream.of(c.getGenericInterfaces()));
		}
		if(t instanceof ParameterizedType){
			ParameterizedType p = (ParameterizedType)t;
			Class<?> raw = (Class<?>)p.getRawType();
			return Stream.concat(Stream.of(p, subst(raw.getGenericSuperclass(), p)), Stream.of(subst(raw.getGenericInterfaces(), p)));
		}
		return Stream.of(t);
	}

	public static boolean isSuper(Type from, Type to) {
		if(isArray(from)){
			return isArray(to) ? isSuper(component(from), component(to)) : Arrays.asList(Object.class, Serializable.class, Cloneable.class).contains(to);
		}
		return supers(from).anyMatch(t -> contains(t, to));
	}
}
