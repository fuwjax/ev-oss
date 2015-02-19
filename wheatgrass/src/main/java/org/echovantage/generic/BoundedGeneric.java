package org.echovantage.generic;

import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

/**
 * Created by fuwjax on 2/18/15.
 */
public class BoundedGeneric implements Generic {
    public BoundedGeneric(WildcardType type) {
        throw new UnsupportedOperationException();
    }

    public BoundedGeneric(TypeVariable type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeVariables library() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAssignableFrom(Generic value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<GenericMember> members() {
        throw new UnsupportedOperationException();
    }
}
