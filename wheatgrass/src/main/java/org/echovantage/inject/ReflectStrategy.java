package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.generic.Spec;
import org.echovantage.util.Types;
import sun.net.www.content.text.Generic;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.generic.GenericMember.TargetType.INSTANCE;

public class ReflectStrategy implements InjectorStrategy {
    private final Map<Type, Set<GenericMember>> bindings = new HashMap<>();
    private final Object obj;

    public ReflectStrategy(final Object obj) {
        this.obj = obj;
        Spec spec = Spec.of(obj.getClass());
        spec.members().filter(INSTANCE.and(PROTECTED).and(m -> !Types.isVoid(m.returnType())).and(m -> Types.isAssignable(obj.getClass(), m.declaringClass()))).forEach(this::register);
    }

    private void register(final GenericMember member) {
        bindings.computeIfAbsent(member.returnType(), k -> new HashSet<>()).add(member);
    }

    @Override
    public Binding bindingFor(BindConstraint constraint) {
        if (Types.isAssignable(constraint.type(), obj.getClass())) {
            return i -> obj;
        }
        Set<GenericMember> possibles;
        if (bindings.containsKey(constraint.type())) {
            possibles = bindings.get(constraint.type());
        }else{
            possibles = new HashSet<>();
            bindings.entrySet().stream().filter(e -> Types.isAssignable(constraint.type(), e.getKey())).map(Map.Entry::getValue).forEach(list -> possibles.addAll(list));
            bindings.put(constraint.type(), possibles);
        }
        final List<GenericMember> assigns = possibles.stream().filter(constraint).collect(Collectors.toList());
        if (assigns.isEmpty()) {
            return null;
        }
        if (assigns.size() == 1) {
            GenericMember m = assigns.get(0);
            return scope -> scope.invoke(obj, m);
        }
        throw new IllegalArgumentException("Multiple bindings for " + constraint);
    }
}
