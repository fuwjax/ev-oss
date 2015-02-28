package org.echovantage.generic;

import org.echovantage.util.Types;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Predicate;

/**
 * Created by fuwjax on 2/18/15.
 */
public abstract class TypeTemplate<T> implements ParameterizedType {
    private final ParameterizedType type;

    protected TypeTemplate() {
        ParameterizedType t = (ParameterizedType) getClass().getGenericSuperclass();
        if (!TypeTemplate.class.equals(t.getRawType())) {
            throw new IllegalStateException("Template instances must be direct anonymous classes");
        }
        Type arg = t.getActualTypeArguments()[0];
        if (!(arg instanceof ParameterizedType)) {
            throw new IllegalStateException("Template anonymous instances should be for generic, non-array types");
        }
        type = (ParameterizedType) arg;
    }

    public AnnotatedType with(Annotation... annotations){
        return Types.annotatedType(this, annotations);
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
    public boolean equals(Object obj) {
        return type.equals(obj);
    }
}
