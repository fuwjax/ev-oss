package org.echovantage.inject;

import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.generic.Spec;
import org.echovantage.util.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.echovantage.generic.GenericMember.TargetType.INSTANCE;
import static org.echovantage.util.function.Functions.function;

public class ReflectInjector extends Injector {
    private final Map<Type, Function<Injector, ?>> providers = new HashMap<>();
    private final Object obj;

    public ReflectInjector(final Object obj) {
        this(obj, MemberAccess.PUBLIC);
    }

    private ReflectInjector(Object obj, MemberAccess access) {
        this.obj = obj;
        Spec spec = Spec.of(obj.getClass());
        register(obj.getClass(), i -> obj);
        spec.members().filter(INSTANCE.and(access)).forEach(m -> register(m.returnType(), function(m::invoke)));
    }

    private void register(final Type type, final Function<Injector, ?> function) {
        final Function<Injector, ?> old = providers.put(type, function);
        if (old != null) {
            throw new IllegalStateException("Multiple bindings for " + type);
        }
    }

    @Override
    protected Object get(final Injector source, final Type type) {
        Function<Injector, ?> provider = providers.get(type);
        if (provider == null) {
            final List<Function<Injector, ?>> assigns = providers.entrySet().stream().filter(e -> Types.isAssignable(type, e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
            if (assigns.size() == 1) {
                provider = assigns.get(0);
            } else if (assigns.size() > 1) {
                throw new IllegalStateException("Multiple bindings for " + type);
            }
        }
        // can't type.cast() here as the type may be primitive
        return provider == null ? null : provider.apply(source);
    }

    @Override
    protected Injector internal() {
        return new ReflectInjector(obj, MemberAccess.PROTECTED);
    }
}
