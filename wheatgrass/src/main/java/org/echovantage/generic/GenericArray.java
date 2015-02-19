package org.echovantage.generic;

import java.lang.reflect.GenericArrayType;
import java.util.List;

/**
 * Created by fuwjax on 2/18/15.
 */
public class GenericArray implements Generic {
    private final Generic component;

    public GenericArray(GenericArrayType type) {
        component = Generic.of(type.getGenericComponentType());
    }

    @Override
    public boolean isAssignableFrom(Generic value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeVariables library() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<GenericMember> members() {
        throw new UnsupportedOperationException();
    }
}
