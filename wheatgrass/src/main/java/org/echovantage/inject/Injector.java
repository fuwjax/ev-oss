package org.echovantage.inject;

import org.echovantage.util.Annotations;

import javax.inject.Named;
import java.lang.reflect.Type;

public class Injector implements ObjectFactory {
    public static Injector newInjector(final Object... modules) throws ReflectiveOperationException {
        InjectorStrategy[] injectors = new InjectorStrategy[modules.length + 1];
        Injector injector = new Injector(new ChainStrategy(injectors));
        injectors[modules.length] = injector::spawn;
        for (int i = 0; i < modules.length; i++) {
            Object module = modules[i] instanceof Type ? injector.get(new BindConstraint((Type) modules[i])) : modules[i];
            injectors[i] = module instanceof InjectorStrategy ? (InjectorStrategy) module : new ReflectStrategy(module);
        }
        return injector;
    }

    public static Named named(String name) {
        return Annotations.of(Named.class, name);
    }

    private Binding spawn(BindConstraint constraint) {
        return scope -> scope.create(constraint);
    }

    private final InjectorStrategy strategy;

    protected Injector(final InjectorStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void inject(BindConstraint constraint, Object target) throws ReflectiveOperationException {
        scope().inject(constraint, target);
    }

    public Scope scope() {
        return new Scope(strategy);
    }

    @Override
    public Object get(BindConstraint constraint) throws ReflectiveOperationException {
        return scope().get(constraint);
    }
}
