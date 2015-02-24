package org.echovantage.generic;

import org.echovantage.util.Members;
import org.echovantage.util.Streams;
import org.echovantage.util.Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Spec {
    private static ConcurrentMap<Type, Spec> specs = new ConcurrentHashMap<>();
    private final Type type;

    public static Spec of(final Type type) {
        if (Types.isInstantiable(type)) {
            return specs.computeIfAbsent(type, Spec::new);
        }
        throw new IllegalArgumentException("Type is not instantiable: " + type);
    }

    private Set<GenericMember> members = new TreeSet<>(GenericMember.COMPARATOR);

    private Spec(Type type) {
        this.type = type;
        if (type instanceof Class) {
            Class<?> cls = (Class<?>) type;
            Arrays.asList(cls.getDeclaredConstructors()).forEach(c -> members.add(new ConstructorMember(c)));
            Arrays.asList(cls.getDeclaredMethods()).forEach(m -> members.add(new MethodMember(m)));
            Arrays.asList(cls.getDeclaredFields()).forEach(f -> members.add(new GetFieldMember(f)));
            Arrays.asList(cls.getDeclaredFields()).forEach(f -> members.add(new SetFieldMember(f)));
            for (Class<?> sup = cls.getSuperclass(); sup != null; sup = sup.getSuperclass()) {
                Arrays.asList(sup.getDeclaredMethods()).stream().filter(Members::isNotStatic).forEach(m -> members.add(new MethodMember(m)));
                Arrays.asList(sup.getDeclaredFields()).stream().filter(Members::isNotStatic).forEach(f -> members.add(new GetFieldMember(f)));
                Arrays.asList(sup.getDeclaredFields()).stream().filter(Members::isNotStatic).forEach(f -> members.add(new SetFieldMember(f)));
            }
        } else {
            ParameterizedType p = (ParameterizedType) type;
            Spec raw = of(p.getRawType());
            for (GenericMember m : Streams.over(raw.members())) {
                members.add(new ResolvedMember(m, p));
            }
        }
    }

    public Stream<GenericMember> members() {
        return members.stream();
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
