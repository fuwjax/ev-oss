package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.generic.Rei;
import org.echovantage.generic.Spec;
import org.echovantage.util.Arrays2;
import org.echovantage.util.RunWrapException;

import java.lang.reflect.Type;

import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.generic.GenericMember.MemberAccess.PUBLIC;
import static org.echovantage.util.function.Functions.function;

public abstract class Injector {
    public static Injector newInjector(final Object... modules) {
        Injector[] injectors = new Injector[modules.length + 1];
        injectors[modules.length] = new SpawnInjector();
        Injector injector = new ChainInjector(injectors);
        for (int i = 0; i < modules.length; i++) {
            Object module = modules[i] instanceof Type ? injector.get(modules[i]) : modules[i];
            injectors[i] = module instanceof Injector ? (Injector) module : new ReflectInjector(module);
        }
        return injector;
    }

    public void inject(final Object object) {
        if (object != null) {
            final InjectSpec spec = InjectSpec.get(object.getClass());
            spec.inject(this, object);
        }
    }

    public <T> T get(final Class<T> type) {
        return (T) get(type, PUBLIC);
    }

    public <T> T get(final Rei<T> type) {
        return (T) get(type, PUBLIC);
    }

    protected Object get(Type type){
        return get(type, PUBLIC);
    }

    protected abstract Object get(Type type, MemberAccess access);

    public Object invoke(GenericMember member, Object target) {
        try{
            Object[] args = Arrays2.transform(member.paramTypes(), function(t -> get(t, PROTECTED)));
            return member.invoke(target, args);
        }catch(ReflectiveOperationException e){
            throw new RunWrapException(e);
        }
    }
}
