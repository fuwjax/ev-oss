package org.echovantage.generic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * Created by fuwjax on 2/18/15.
 */
public abstract class Rei<T> implements ParameterizedType {
    private final ParameterizedType type;

    protected Rei(){
        ParameterizedType t = (ParameterizedType)getClass().getGenericSuperclass();
        if(!Rei.class.equals(t.getRawType())){
            throw new IllegalStateException("Rei instances must be direct anonymous classes");
        }
        Type arg = t.getActualTypeArguments()[0];
        if(!(arg instanceof ParameterizedType)){
            throw new IllegalStateException("Rei anonymous instances should be for generic types");
        }
        type = (ParameterizedType)arg;
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
