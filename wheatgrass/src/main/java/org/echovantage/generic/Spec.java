package org.echovantage.generic;

import org.echovantage.util.Members;
import org.echovantage.util.Streams;
import org.echovantage.util.Types;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.echovantage.util.function.Functions.function;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Spec{
    private static ConcurrentMap<Type, Spec> specs = new ConcurrentHashMap<>();

    public static Spec of(final Type type) {
        if(Types.isInstantiable(type)){
            return specs.computeIfAbsent(type, Spec::new);
        }
        throw new IllegalArgumentException("Type is not instantiable: "+type);
    }

    private Set<GenericMember> members = new TreeSet<>();

    private Spec(Type t) {
        if(t instanceof Class){
            Class<?> type = (Class<?>)t;
            Arrays.asList(type.getDeclaredConstructors()).forEach(c -> members.add(new ConstructorMember(c)));
            Arrays.asList(type.getDeclaredMethods()).forEach(m -> members.add(new MethodMember(m)));
            Arrays.asList(type.getDeclaredFields()).forEach(f -> members.add(new GetFieldMember(f)));
            Arrays.asList(type.getDeclaredFields()).forEach(f -> members.add(new SetFieldMember(f)));
            for(Class<?> cls = type.getSuperclass(); cls != null; cls = cls.getSuperclass()){
                Arrays.asList(cls.getDeclaredMethods()).stream().filter(Members::isNotStatic).forEach(m -> members.add(new MethodMember(m)));
                Arrays.asList(cls.getDeclaredFields()).stream().filter(Members::isNotStatic).forEach(f -> members.add(new GetFieldMember(f)));
                Arrays.asList(cls.getDeclaredFields()).stream().filter(Members::isNotStatic).forEach(f -> members.add(new SetFieldMember(f)));
            }
        }else{
            ParameterizedType p = (ParameterizedType)t;
            Spec raw = of(p.getRawType());
            for(GenericMember m: Streams.over(raw.members())){
                Type[] params = Types.subst(m.paramTypes(), p);
                Type returns = Types.subst(m.returnType(), p);
                members.add(new ResolvedMember(m, params, returns));
            }
        }
    }

    public Stream<GenericMember> members() {
        return members.stream();
    }
}
