package org.echovantage.generic;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Spec implements Generic{
    public static Spec of(Class<?> type){
        return type == null ? null : new Spec(type);
    }

    private final Spec owner;

    private Spec(Class<?> type) {
        owner = of(type.getDeclaringClass());
    }

    @Override
    public boolean isAssignableFrom(Generic value) {
        return false;
    }

    @Override
    public List<GenericMember> members() {
        return null;
    }

    public List<GenericMember> membersFor(TypeVariables library) {
        return null;
    }
}
