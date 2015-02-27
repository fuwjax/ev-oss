package org.echovantage.inject;

import org.echovantage.generic.GenericMember;

import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/20/15.
 */
public class ChainStrategy implements InjectorStrategy {
    private final InjectorStrategy[] injectors;

    public ChainStrategy(InjectorStrategy... injectors) {
        this.injectors = injectors;
    }

    @Override
    public Binding bindingFor(BindConstraint constraint) {
        for (final InjectorStrategy injector : injectors) {
            if (injector != null) {
                final Binding result = injector.bindingFor(constraint);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
