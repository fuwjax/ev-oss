package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.Spec;
import org.echovantage.util.RunWrapException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.echovantage.generic.GenericMember.MemberType.CONSTRUCTOR;
import static org.echovantage.generic.GenericMember.TargetType.INSTANCE;

public class InjectSpec {
    private static ConcurrentMap<Type, InjectSpec> specs = new ConcurrentHashMap<>();

    public static InjectSpec get(final Type type) {
        return type == null ? null : specs.computeIfAbsent(type, t -> new InjectSpec(Spec.of(t)));
    }

    private final GenericMember constructor;
    private final List<GenericMember> members;

    private InjectSpec(final Spec type) {
        constructor = constructor(type);
        members = type.members().filter(INSTANCE.and(InjectSpec::isInject)).collect(Collectors.toList());
    }

    private static GenericMember constructor(final Spec type) {
        final List<GenericMember> constructors = type.members().filter(CONSTRUCTOR).collect(Collectors.toList());
        if (constructors.size() == 0) {
            throw new IllegalArgumentException("Inject specifications require " + type + " to be constructible");
        }
        GenericMember best;
        if (constructors.size() == 1) {
            best = constructors.get(0);
        } else {
            final List<GenericMember> injects = constructors.stream().filter(InjectSpec::isInject).collect(Collectors.toList());
            if (injects.isEmpty()) {
                best = constructors.stream().filter(m -> m.paramTypes().length == 0).findAny().get();
            } else if (injects.size() == 1) {
                best = injects.get(0);
            } else {
                throw new IllegalArgumentException("Inject specifications require a single @Inject constructor in " + type);
            }
        }
        return best;
    }

    private static boolean isInject(final GenericMember member) {
        for (final Annotation anno : member.annotations()) {
            if ("Inject".equals(anno.annotationType().getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    public void inject(final Injector source, final Object object) {
        try {
            for (GenericMember member : members) {
                member.invoke(source, object);
            }
        } catch (ReflectiveOperationException e) {
            throw new RunWrapException(e);
        }
    }

    public Object create(final Injector source) {
        try {
            final Object o = constructor.invoke(source);
            inject(source, o);
            return o;
        } catch (ReflectiveOperationException e) {
            throw new RunWrapException(e);
        }
    }
}
