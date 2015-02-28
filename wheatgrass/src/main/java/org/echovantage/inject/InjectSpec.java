package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.Spec;
import org.echovantage.util.RunWrapException;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.echovantage.generic.GenericMember.MemberType.CONSTRUCTOR;
import static org.echovantage.generic.GenericMember.TargetType.INSTANCE;
import static org.echovantage.util.function.Functions.function;

public class InjectSpec {
    private static ConcurrentMap<Type, InjectSpec> specs = new ConcurrentHashMap<>();

    public static InjectSpec of(final Type type) throws ReflectiveOperationException {
        try {
            return specs.computeIfAbsent(type, function(InjectSpec::new));
        } catch (RunWrapException e) {
            throw e.throwIf(ReflectiveOperationException.class);
        }
    }

    private final GenericMember constructor;
    private final List<GenericMember> members;

    private InjectSpec(final Type type) throws ReflectiveOperationException {
        Spec spec = Spec.of(type);
        constructor = constructor(spec);
        members = spec.members().filter(INSTANCE.and(InjectSpec::isInject)).collect(Collectors.toList());
    }

    private static GenericMember constructor(final Spec type) throws ReflectiveOperationException {
        final List<GenericMember> constructors = type.members().filter(CONSTRUCTOR).collect(Collectors.toList());
        if (constructors.size() == 0) {
            return null;
        }
        if (constructors.size() == 1) {
            return constructors.get(0);
        }
        final List<GenericMember> injects = constructors.stream().filter(InjectSpec::isInject).collect(Collectors.toList());
        if (injects.isEmpty()) {
            return constructors.stream().filter(m -> m.paramTypes().length == 0).findAny().orElse(null);
        }
        if (injects.size() == 1) {
            return injects.get(0);
        }
        return null;
    }

    private static boolean isInject(final GenericMember member) {
        return member.source().isAnnotationPresent(Inject.class);
    }

    public Stream<GenericMember> members() {
        return members.stream();
    }

    public GenericMember constructor() {
        if (constructor == null) {
            throw new IllegalArgumentException("Inject specifications require a single @Inject constructor");
        }
        return constructor;
    }
}
