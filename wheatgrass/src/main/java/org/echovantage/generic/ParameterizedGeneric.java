package org.echovantage.generic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * Created by fuwjax on 2/18/15.
 */
public class ParameterizedGeneric implements Generic {
    private final Spec type;
    private final TypeVariables args;
    private final Generic owner; // either null, a ParameterizedGeneric or a Spec

    public ParameterizedGeneric(ParameterizedType parameterizedType) {
        owner = Generic.of(parameterizedType.getOwnerType());
        type = Spec.of((Class<?>)parameterizedType.getRawType());
        args = owner == null ? new TypeVariables(parameterizedType) : owner.library().with(parameterizedType);
    }

    protected ParameterizedGeneric(){
        ParameterizedType t = (ParameterizedType)getClass().getGenericSuperclass();
        if(!Rei.class.equals(t.getRawType())){
            throw new IllegalStateException("Rei instances must be direct anonymous classes");
        }
        Type arg = t.getActualTypeArguments()[0];
        if(!(arg instanceof ParameterizedType)){
            throw new IllegalStateException("Rei anonymous instances should be for generic types");
        }
        ParameterizedType parameterizedType = (ParameterizedType)arg;
        owner = Generic.of(parameterizedType.getOwnerType());
        type = Spec.of((Class<?>)parameterizedType.getRawType());
        args = owner == null ? new TypeVariables(parameterizedType) : owner.library().with(parameterizedType);
    }

    public boolean isAssignableFrom(Generic value) {
        return type.isAssignableFrom(value);
    }

    @Override
    public TypeVariables library() {
        return args;
    }

    @Override
    public List<GenericMember> members() {
        return type.membersFor(args);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Generic)){
            return false;
        }
        Generic o = (Generic)obj;
        return isAssignableFrom(o) && o.isAssignableFrom(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, args);
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
