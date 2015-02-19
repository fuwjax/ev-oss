package org.echovantage.generic;

import java.lang.reflect.*;
import java.util.List;

/**
 * Created by fuwjax on 2/18/15.
 */
public interface Generic {
    public static Generic of(Type type) {
        if(type == null){
            return null;
        }
        if(type instanceof Class){
            //TODO handle array
            return Spec.of((Class<?>)type);
        }
        if(type instanceof ParameterizedType){
            return new ParameterizedGeneric((ParameterizedType)type);
        }
        if(type instanceof GenericArrayType){
            return new GenericArray((GenericArrayType)type);
        }
        if(type instanceof WildcardType){
            return new BoundedGeneric((WildcardType)type);
        }
        if(type instanceof TypeVariable){
            return new BoundedGeneric((TypeVariable)type);
        }
        throw new IllegalArgumentException("Unknown type "+type.getClass());
    }

    boolean isAssignableFrom(Generic value);

    List<GenericMember> members();

    TypeVariables library();
}
