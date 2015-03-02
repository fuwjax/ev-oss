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
import org.echovantage.generic.TypeTemplate;
import org.echovantage.util.Arrays2;
import org.echovantage.util.RunWrapException;
import org.echovantage.util.Streams;
import org.echovantage.util.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.util.function.Functions.function;

/**
 * Created by fuwjax on 2/17/15.
 */
public class Scope implements ObjectFactory {
    private final Map<BindConstraint, Object> objects = new HashMap<>();
    private final InjectorStrategy injector;

    public Scope(InjectorStrategy injector) {
        this.injector = injector;
        objects.put(new BindConstraint(ObjectFactory.class), this);
    }

    @Override
    public Object get(BindConstraint constraint) throws ReflectiveOperationException {
        Object object = objects.get(constraint);
        if (object == null) {
            object = binding(constraint).get(this);
            objects.put(constraint, object);
        }
        return object;
    }

    @Override
    public void inject(BindConstraint constraint, Object target) throws ReflectiveOperationException {
        assert constraint != null;
        assert target != null;
        objects.put(constraint, target);
        inject(InjectSpec.of(constraint.type()), target);
    }

    public Object invoke(Object target, GenericMember member) throws ReflectiveOperationException {
        try {
            Object[] args = Arrays2.transform(member.paramTypes(), new Object[0], function(t -> get(new BindConstraint(t))));
            return member.invoke(target, args);
        }catch(RunWrapException e){
            throw e.throwIf(ReflectiveOperationException.class);
        }
    }

    private Binding binding(BindConstraint constraint) throws ReflectiveOperationException {
        Binding binding = injector.bindingFor(constraint);
        if(binding == null) {
            throw new ReflectiveOperationException("No binding for " + constraint);
        }
        return binding;
    }

    public Object create(BindConstraint constraint) throws ReflectiveOperationException {
        InjectSpec spec = InjectSpec.of(constraint.type());
        final Object o = invoke(null, spec.constructor());
        objects.put(constraint, o);
        inject(spec, o);
        return o;
    }

    private void inject(InjectSpec spec, Object object) throws ReflectiveOperationException {
        for (GenericMember member : Streams.over(spec.members())) {
            invoke(object, member);
        }
    }

}
