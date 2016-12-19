/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.inject;

import static org.fuwjax.oss.generic.GenericMember.MemberAccess.PROTECTED;
import static org.fuwjax.oss.generic.GenericMember.TargetType.INSTANCE;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.fuwjax.oss.generic.GenericMember;
import org.fuwjax.oss.generic.Spec;
import org.fuwjax.oss.util.Types;

public class ReflectStrategy implements BindingStrategy {
    private final Map<Type, Set<GenericMember>> bindings = new HashMap<>();
    private final Object obj;

    public ReflectStrategy(final Object obj, InjectionLibrary library) {
        this.obj = obj;
        Spec spec = library.spec(obj.getClass());
        spec.members().filter(INSTANCE.and(PROTECTED).and(m -> !Types.isVoid(m.returnType().type())).and(m -> Types.isAssignable(obj.getClass(), m.declaringClass().type()))).forEach(this::register);
    }

    private void register(final GenericMember member) {
        bindings.computeIfAbsent(member.returnType().type(), k -> new HashSet<>()).add(member);
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
            bindings.entrySet().stream().filter(e -> Types.isAssignable(constraint.type(), e.getKey())).map(Map.Entry::getValue).forEach(possibles::addAll);
            bindings.put(constraint.type(), possibles);
        }
        final List<GenericMember> assigns = possibles.stream().map(constraint::coerce).filter(e -> e != null).collect(Collectors.toList());
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
