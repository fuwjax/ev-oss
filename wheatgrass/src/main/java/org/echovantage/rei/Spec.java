package org.echovantage.rei;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Spec implements Generic{
    public static Spec of(Class<?> type){
        return new Spec(type);
    }

    private Spec(Class<?> type) {

    }

    public Generic[] args(Type[] args) {
        return new Generic[0];
    }

    @Override
    public boolean isAssignableFrom(Generic value) {
        return false;
    }

    @Override
    public List<GenericMember> members() {
        return null;
    }

    public List<GenericMember> membersFor(Generic[] args) {
        return null;
    }
}
