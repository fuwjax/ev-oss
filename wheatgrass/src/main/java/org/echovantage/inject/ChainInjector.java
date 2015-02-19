package org.echovantage.inject;

import org.echovantage.generic.Generic;

public class ChainInjector extends Injector {
	private final Injector[] injectors;

	public ChainInjector(final Injector... injectors) {
		this.injectors = injectors;
	}

	@Override
	protected Object get(final Injector source, final Generic type) throws ReflectiveOperationException {
		for(final Injector injector : injectors) {
            if(injector != null) {
                final Object result = injector.get(source, type);
                if (result != null) {
                    return result;
                }
            }
		}
		return null;
	}

    @Override
    protected Injector internal() {
        Injector[] subs = new Injector[injectors.length];
        for(int i=0;i<injectors.length;i++){
            subs[i] = injectors[i] == null ? null : injectors[i].internal();
        }
        return new ChainInjector(subs);
    }
}
