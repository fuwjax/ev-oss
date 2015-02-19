package org.echovantage.rei;

import org.echovantage.util.Arrays2;
import org.echovantage.util.Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Rei<T> implements Generic{
    private Spec type;
    private Generic[] args;

    public Rei(){
        ParameterizedType t = (ParameterizedType)getClass().getGenericSuperclass();
        if(!Rei.class.equals(t.getRawType())){
            throw new IllegalStateException("Rei instances must be direct anonymous classes");
        }
        Type arg = t.getActualTypeArguments()[0];
        if(!(arg instanceof ParameterizedType)){
            throw new IllegalStateException("Rei anonymous instances should be for generic types");
        }
        ParameterizedType p = (ParameterizedType)arg;
        type = Spec.of((Class<?>) p.getRawType());
        args = type.args(p.getActualTypeArguments());
    }

    private Rei(Spec type, Generic... args){
        this.type = type;
        this.args = args;
    }

    public boolean isAssignableFrom(Generic value) {
        return type.isAssignableFrom(value);
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
