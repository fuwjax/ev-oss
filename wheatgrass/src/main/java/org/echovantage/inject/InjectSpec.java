package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.Spec;
import org.echovantage.generic.TypeTemplate;
import org.echovantage.util.RunWrapException;
import org.echovantage.util.Streams;

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
            throw new ReflectiveOperationException("Inject specifications require " + type + " to be constructible");
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
        return member.annotation(Inject.class).length > 0;
    }

    public Stream<GenericMember> members(){
        return members.stream();
    }

    public GenericMember constructor(){
        return constructor;
    }
}
