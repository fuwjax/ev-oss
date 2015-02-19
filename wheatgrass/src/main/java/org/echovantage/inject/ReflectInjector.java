package org.echovantage.inject;

import static org.echovantage.util.Members.fields;
import static org.echovantage.util.Members.methods;
import static org.echovantage.util.function.Functions.function;
import static org.echovantage.util.function.Functions.predicate;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.echovantage.rei.Generic;
import org.echovantage.rei.GenericMember;
import org.echovantage.rei.GenericMember.MemberAccess;
import org.echovantage.rei.Rei;
import org.echovantage.rei.Spec;
import org.echovantage.util.Members;
import org.echovantage.util.function.Functions;

public class ReflectInjector extends Injector {
	private final Map<Generic, Function<Injector, ?>> providers = new HashMap<>();
    private final Object obj;

    public ReflectInjector(final Object obj) {
        this(obj, MemberAccess.PUBLIC);
    }

    private ReflectInjector(Object obj, MemberAccess access){
        this.obj = obj;
        Spec spec = Spec.of(obj.getClass());
        register(spec, i -> obj);
        Predicate<GenericMember> real = predicate(GenericMember::isStatic).negate().and(m -> m.access() == access);
        spec.members().stream().filter(real).forEach(m -> register(m.returnType(), m::invoke));
	}

	private void register(final Generic type, final Function<Injector, ?> function) {
		final Function<Injector, ?> old = providers.put(type, function);
		if(old != null) {
			throw new IllegalStateException("Multiple bindings for " + type);
		}
	}

	@Override
	protected Object get(final Injector source, final Generic type) throws ReflectiveOperationException {
		Function<Injector, ?> provider = providers.get(type);
		if(provider == null) {
			final List<Function<Injector, ?>> assigns = providers.entrySet().stream().filter(e -> type.isAssignableFrom(e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
			if(assigns.size() == 1) {
				provider = assigns.get(0);
			} else if(assigns.size() > 1) {
				throw new IllegalStateException("Multiple bindings for " + type);
			}
		}
		// can't type.cast() here as the type may be primitive
		return provider == null ? null : provider.apply(source);
	}

    @Override
    protected Injector internal() {
        return new ChainInjector(this, new ReflectInjector(obj, MemberAccess.PROTECTED));
    }
}
