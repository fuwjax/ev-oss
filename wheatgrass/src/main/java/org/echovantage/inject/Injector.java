package org.echovantage.inject;

import org.echovantage.rei.Generic;
import org.echovantage.rei.Rei;
import org.echovantage.rei.Spec;
import org.echovantage.util.Arrays2;

import static org.echovantage.util.function.Functions.function;

public abstract class Injector {
    public static Injector newInjector(final Object... modules) throws ReflectiveOperationException {
        Injector[] injectors = new Injector[modules.length + 1];
        injectors[modules.length] = new SpawnInjector();
        Injector injector = new ChainInjector(injectors);
        for (int i = 0; i < modules.length; i++) {
            Object module = modules[i] instanceof Class ? injector.get((Class<?>) modules[i]) : modules[i];
            injectors[i] = module instanceof Injector ? (Injector) module : new ReflectInjector(module);
        }
        return injector;
    }

    public void inject(final Object object) throws ReflectiveOperationException {
        if (object != null) {
            final InjectSpec spec = InjectSpec.get(Spec.of(object.getClass()));
            spec.inject(buffer(object), object);
        }
    }

    protected Injector buffer(Object object) {
        return this instanceof BufferedInjector ? this : new BufferedInjector(internal(), object);
    }

    protected abstract Injector internal();

    public <T> T get(final Class<T> type) throws ReflectiveOperationException {
        return (T)get(this, Spec.of(type));
    }

    public <T> T get(final Rei<T> type) throws ReflectiveOperationException {
        return (T) get(this, type);
    }

    protected abstract Object get(Injector source, Generic type) throws ReflectiveOperationException;

    public Object[] get(final Generic[] types) throws ReflectiveOperationException {
        Injector injector = buffer(null);
        return Arrays2.transform(types, function(t -> get(injector, t)));
    }
}
