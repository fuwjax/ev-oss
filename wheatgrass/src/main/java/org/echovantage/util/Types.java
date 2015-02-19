package org.echovantage.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Types {
    public static Class<?> asClass(Type type){
        if(type instanceof Class){
            return (Class<?>) type;
        }
        if(type instanceof ParameterizedType){
            return asClass(((ParameterizedType) type).getRawType());
        }
        if(type instanceof GenericArrayType){
            return arrayOf(asClass(((GenericArrayType) type).getGenericComponentType()));
        }
        if(type instanceof WildcardType){
            return asClass(((WildcardType) type).getUpperBounds()[0]);
        }
        if(type instanceof TypeVariable){
            return asClass(((TypeVariable) type).getBounds()[0]);
        }
        throw new UnsupportedOperationException("Unknown type "+type.getClass());
    }

    public static Class<?> arrayOf(Class<?> component){
        if(component.isPrimitive()){
            throw new UnsupportedOperationException("Primitive types not yet supported");
        }
        try {
            String name = component.isArray() ? "["+component.getName() : "[L"+component.getName()+";";
            return component.getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RunWrapException(e);
        }
    }
}
