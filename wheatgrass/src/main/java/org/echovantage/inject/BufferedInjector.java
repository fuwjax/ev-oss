package org.echovantage.inject;

import org.echovantage.rei.Generic;
import org.echovantage.rei.Rei;
import org.echovantage.util.function.Functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.echovantage.util.function.Functions.function;

/**
 * Created by fuwjax on 2/17/15.
 */
public class BufferedInjector extends Injector {
    private final Map<Generic, Object> objects = new HashMap<>();
    private final Injector injector;

    public BufferedInjector(Injector injector, Object object) {
        this.injector = injector;
        if(object != null){
            objects.put(Generic.of(object.getClass()), object);
        }
    }

    @Override
    protected Injector internal() {
        return injector;
    }

    @Override
    protected Object get(Injector source, Generic type) throws ReflectiveOperationException {
        Object object = objects.get(type);
        if(object == null) {
            final List<Object> assigns = objects.entrySet().stream().filter(e -> type.isAssignableFrom(e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
            if(assigns.size() == 1) {
                object = assigns.get(0);
            } else if(assigns.size() > 1) {
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
