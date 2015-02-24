package org.echovantage.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public static boolean isAssignable(Type lhs, Type rhs) {
        return (rhs == null || lhs != null) && (isAssignable(componentType(lhs), componentType(rhs), true) || isAssignable(lhs, rhs, true) || isAssignable(lhs, box.get(rhs), true));
    }

    private static boolean isAssignable(Type lhs, Type rhs, boolean allowUnchecked){
        if(rhs == null){
            return false;
        }
        if(lhs.equals(rhs)){
            return true;
        }
        if(rhs instanceof Class) {
            Class<?> right = c(rhs);
            if (allowUnchecked && lhs instanceof ParameterizedType) {
                if (p(lhs).getRawType().equals(rhs)) {
                    return true;
                }
            }
            if (isAssignable(lhs, superType(right), allowUnchecked)) {
                return true;
            }
            for(Type iface: right.getGenericInterfaces()){
                if(isAssignable(lhs, iface, allowUnchecked)){
                    return true;
                }
            }
        }else if(rhs instanceof ParameterizedType){
            ParameterizedType right = p(rhs);
            if(isAssignable(lhs, right.getRawType(), false)){
                return true;
            }
            Class<?> self = c(right.getRawType());
            if(isAssignable(lhs, subst(superType(self), right), allowUnchecked)){
                return true;
            }
            for(Type iface: self.getGenericInterfaces()){
                if(isAssignable(lhs, subst(iface, right), allowUnchecked)){
                    return true;
                }
            }
            if(isAssignable(lhs, capture(right), allowUnchecked)){
                return true;
            }
            if(lhs instanceof ParameterizedType){
                ParameterizedType left = p(lhs);
                if(left.getRawType().equals(right.getRawType()) && contains(left.getActualTypeArguments(), right.getActualTypeArguments())){
                    return true;
                }
            }
        }else if(rhs instanceof GenericArrayType){
            if (isAssignable(lhs, Object.class, allowUnchecked)) {
                return true;
            }
            for(Type iface: Object[].class.getGenericInterfaces()){
                if(isAssignable(lhs, iface, allowUnchecked)){
                    return true;
                }
            }
        }else if(rhs instanceof TypeVariable){
            TypeVariable right = v(rhs);
            for(Type bound: right.getBounds()){
                if(isAssignable(lhs, bound, allowUnchecked)){
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

    public static Type subst(Type t, ParameterizedType mapping){
        Type result;
        if(t instanceof TypeVariable){
            TypeVariable v = v(t);
            Class<?> raw = c(mapping.getRawType());
            int index = Arrays.asList(raw.getTypeParameters()).indexOf(v);
            if(index == -1){
                if(mapping.getOwnerType() instanceof ParameterizedType){
                    return subst(t, p(mapping.getOwnerType()));
                }
                throw new IllegalArgumentException("Variable "+t+" is not present in "+mapping);
            }
            Type arg = mapping.getActualTypeArguments()[index];
            result = subst(arg == null ? wildcardOf(v.getBounds(), NO_PARAMS) : arg, mapping);
        }else if(t instanceof GenericArrayType){
            final GenericArrayType array = a(t);
            Type comp = subst(array.getGenericComponentType(), mapping);
            result = arrayOf(comp);
        }else if(t instanceof ParameterizedType){
            ParameterizedType p = p(t);
            Type owner = subst(p.getOwnerType(), mapping);
            Type[] args = subst(p.getActualTypeArguments(), mapping);
            result = paramOf(owner, p.getRawType(), args);
        }else if(t instanceof WildcardType){
            WildcardType w = w(t);
            Type[] upper = subst(w.getUpperBounds(), mapping);
            Type[] lower = subst(w.getLowerBounds(), mapping);
            result = wildcardOf(upper, lower);
        }else if(t instanceof Class){
            result = t;
        }else{
            throw new IllegalArgumentException("Unknown type "+t);
        }
        return result.equals(t) ? t : result;
    }

    public static Type[] subst(Type[] types, ParameterizedType mapping){
        return Arrays2.transform(types, NO_PARAMS, t -> subst(t, mapping));
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

            public boolean equals(Object obj) {
                if(obj instanceof ParameterizedType) {
                    ParameterizedType o = p((Type)obj);
                    return Objects.equals(owner, o.getOwnerType()) && Objects.equals(raw, o.getRawType()) && Arrays.equals(args, o.getActualTypeArguments());
                }
                return false;
            }

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

            public boolean equals(Object obj) {
                if(obj instanceof WildcardType) {
                    WildcardType o = w((Type)obj);
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

    private static TypeVariable v(Type t){
        return (TypeVariable)t;
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
}
