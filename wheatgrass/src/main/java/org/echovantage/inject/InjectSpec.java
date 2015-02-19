package org.echovantage.inject;

import static org.echovantage.rei.GenericMember.MemberType.CONSTRUCTOR;
import static org.echovantage.util.Members.access;
import static org.echovantage.util.Members.constructors;
import static org.echovantage.util.Members.fields;
import static org.echovantage.util.Members.methods;
import static org.echovantage.util.function.Functions.consumer;
import static org.echovantage.util.function.Functions.function;
import static org.echovantage.util.function.Functions.predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.echovantage.rei.Generic;
import org.echovantage.rei.GenericMember;
import org.echovantage.rei.Rei;
import org.echovantage.util.RunWrapException;
import org.echovantage.util.function.Functions;

public class InjectSpec {
	private static ConcurrentMap<Generic, InjectSpec> specs = new ConcurrentHashMap<>();
    public static InjectSpec get(final Generic type) throws ReflectiveOperationException {
		if(type == null) {
			return null;
		}
		try {
			return specs.computeIfAbsent(type, function(InjectSpec::new));
		} catch(final RunWrapException e) {
			throw e.throwIf(ReflectiveOperationException.class);
		}
	}

    private final GenericMember constructor;
    private final List<GenericMember> members;

	private InjectSpec(final Generic type) throws ReflectiveOperationException {
        constructor = constructor(type);
        members = type.members().stream().filter(predicate(GenericMember::isStatic).negate().and(InjectSpec::isInject)).collect(Collectors.toList());
	}

	private static GenericMember constructor(final Generic type) throws ReflectiveOperationException {
		final List<GenericMember> constructors = type.members().stream().filter(m -> m.type() == CONSTRUCTOR).collect(Collectors.toList());
		if(constructors.size() == 0) {
			throw new IllegalArgumentException("Inject specifications require " + type + " to be constructible");
		}
		GenericMember best;
		if(constructors.size() == 1) {
			best = constructors.get(0);
		} else {
			final List<GenericMember> injects = constructors.stream().filter(InjectSpec::isInject).collect(Collectors.toList());
			if(injects.isEmpty()) {
				best = constructors.stream().filter(m -> m.paramTypes().length == 0).findAny().get();
			} else if(injects.size() == 1) {
				best = injects.get(0);
			} else {
				throw new IllegalArgumentException("Inject specifications require a single @Inject constructor in " + type);
			}
		}
        return best;
	}

	private static boolean isInject(final GenericMember member) {
		for(final Annotation anno : member.getAnnotations()) {
			if("Inject".equals(anno.annotationType().getSimpleName())) {
				return true;
			}
		}
		return false;
	}

	public void inject(final Injector source, final Object object) throws ReflectiveOperationException {
        for(GenericMember member: members){
            member.invoke(source, object);
        }
	}

	public Object create(final Injector source) throws ReflectiveOperationException {
		final Object o = constructor.invoke(source);
        inject(source, o);
		return o;
	}
}
