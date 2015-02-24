/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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
package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.generic.Spec;
import org.echovantage.util.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.generic.GenericMember.TargetType.INSTANCE;

public class ReflectStrategy implements InjectorStrategy {
    private final Map<Type, GenericMember> bindings = new HashMap<>();
    private final Object obj;

    public ReflectStrategy(final Object obj) {
        this.obj = obj;
        Spec spec = Spec.of(obj.getClass());
        spec.members().filter(INSTANCE.and(PROTECTED).and(m -> !Types.isVoid(m.returnType())).and(m -> Types.isAssignable(obj.getClass(), m.declaringClass()))).forEach(this::register);
    }

    private void register(final GenericMember binding) {
        final GenericMember old = bindings.put(binding.returnType(), binding);
        if (old != null) {
            throw new IllegalStateException("Multiple bindings for " + binding.returnType());
        }
    }

    @Override
    public Binding bindingFor(Type type, MemberAccess access) {
        if (Types.isAssignable(type, obj.getClass())) {
            return i -> obj;
        }
        GenericMember binding = bindings.get(type);
        if (binding != null) {
            return access.test(binding) ? i -> i.invoke(obj, binding) : null;
        }
        final List<GenericMember> assigns = bindings.entrySet().stream().filter(e -> access.test(e.getValue()) && Types.isAssignable(type, e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
        if (assigns.isEmpty()) {
            return null;
        }
        if (assigns.size() == 1) {
            return assigns.get(0)::invoke;
        }
        throw new IllegalArgumentException("Multiple bindings for " + type);
    }
}
