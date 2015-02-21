package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.util.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by fuwjax on 2/17/15.
 */
public class BufferedFactory implements ObjectFactory {
    private final Map<Type, Object> objects = new HashMap<>();
    private final InjectorStrategy injector;

    public BufferedFactory(InjectorStrategy injector) {
        this.injector = injector;
    }

    @Override
    public Object get(Type type, MemberAccess access) throws ReflectiveOperationException {
        Object object = objects.get(type);
        if (object == null) {
            final List<Object> assigns = objects.entrySet().stream().filter(e -> Types.isAssignable(type, e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
            if (assigns.size() == 1) {
                object = assigns.get(0);
            } else if (assigns.size() > 1) {
                throw new IllegalArgumentException("Multiple bindings for " + type);
            } else {
                object = injector.get(this, type, access);
            }
            buffer(type, object);
        }
        return object;
    }

    private void buffer(Type type, Object object) {
        Object old = objects.put(type, object);
        if(old != null && old != object){
            throw new IllegalArgumentException("Multiple bindings for "+type);
        }
    }

    @Override
    public Object invoke(Object target, GenericMember member) throws ReflectiveOperationException {
        if(target != null){
            buffer(target.getClass(), target);
        }
        return ObjectFactory.super.invoke(target, member);
    }
}
