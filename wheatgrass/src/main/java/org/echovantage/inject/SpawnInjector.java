package org.echovantage.inject;

import org.echovantage.generic.Generic;

public class SpawnInjector extends Injector {
    @Override
    protected Injector internal() {
        return this;
    }

    @Override
	protected Object get(final Injector source, final Generic type) throws ReflectiveOperationException {
		final InjectSpec spec = InjectSpec.get(type);
		return spec.create(source);
	}
}
