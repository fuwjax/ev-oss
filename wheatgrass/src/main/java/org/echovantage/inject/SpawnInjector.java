package org.echovantage.inject;

import java.lang.reflect.Type;

public class SpawnInjector extends Injector {
    @Override
    protected Injector internal() {
        return this;
    }

    @Override
    protected Object get(final Injector source, final Type type) {
        final InjectSpec spec = InjectSpec.get(type);
        return spec.create(source);
    }
}
