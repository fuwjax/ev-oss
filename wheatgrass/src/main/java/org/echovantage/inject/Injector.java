package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.generic.Rei;
import org.echovantage.util.Streams;

import java.lang.reflect.Type;

import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.generic.GenericMember.MemberAccess.PUBLIC;

public class Injector implements ObjectFactory{
    public static Injector newInjector(final Object... modules) throws ReflectiveOperationException {
        InjectorStrategy[] injectors = new InjectorStrategy[modules.length + 1];
        injectors[modules.length] = new SpawnInjector();
        Injector injector = new Injector(new ChainStrategy(injectors));
        for (int i = 0; i < modules.length; i++) {
            Object module = modules[i] instanceof Type ? injector.get((Type)modules[i], PUBLIC) : modules[i];
            injectors[i] = module instanceof InjectorStrategy ? (InjectorStrategy) module : new ReflectInjector(module);
        }
        return injector;
    }

    private final InjectorStrategy strategy;

    protected Injector(final InjectorStrategy strategy) {
        this.strategy = strategy;
    }

    public void inject(final Object object) throws ReflectiveOperationException {
        if (object != null) {
            ObjectFactory factory = factory();
            final InjectSpec spec = InjectSpec.get(object.getClass());
            if(spec == null){
                throw new IllegalArgumentException("Object type is not injectable");
            }
            for (GenericMember member : Streams.over(spec.members())) {
                factory.invoke(object, member);
            }
        }
    }

    public <T> T get(final Class<T> type) throws ReflectiveOperationException {
        return (T) get(type, PUBLIC);
    }

    public <T> T get(final Rei<T> type) throws ReflectiveOperationException {
        return (T) get(type, PUBLIC);
    }

    protected ObjectFactory factory(){
        return new BufferedFactory(strategy);
    }

    public Object get(Type type, MemberAccess access) throws ReflectiveOperationException {
        return strategy.get(factory(), type, access);
    }
}
