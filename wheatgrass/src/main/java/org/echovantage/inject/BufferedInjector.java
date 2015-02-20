package org.echovantage.inject;

import org.echovantage.util.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by fuwjax on 2/17/15.
 */
public class BufferedInjector extends Injector {
    private final Map<Type, Object> objects = new HashMap<>();
    private final Injector injector;

    public BufferedInjector(Injector injector, Object object) {
        this.injector = injector;
        if (object != null) {
            objects.put(object.getClass(), object);
        }
    }

    @Override
    protected Object get(Injector source, Type type) {
        Object object = objects.get(type);
        if (object == null) {
            final List<Object> assigns = objects.entrySet().stream().filter(e -> Types.isAssignable(type, e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
            if (assigns.size() == 1) {
                object = assigns.get(0);
            } else if (assigns.size() > 1) {
                throw new IllegalStateException("Multiple bindings for " + type);
            } else {
                object = injector.get(source, type);
            }
            objects.put(type, object);
        }
        // can't type.cast() here as the type may be primitive
        return object;
    }
}
