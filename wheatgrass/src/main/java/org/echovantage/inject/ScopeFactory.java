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
import org.echovantage.util.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by fuwjax on 2/17/15.
 */
public class ScopeFactory implements ObjectFactory {
    private final Map<Type, Object> objects = new HashMap<>();
    private final InjectorStrategy injector;

    public ScopeFactory(InjectorStrategy injector) {
        this.injector = injector;
        objects.put(ObjectFactory.class, this);
    }

    @Override
    public Object get(Type type, MemberAccess access) throws ReflectiveOperationException {
        Object object = objects.get(type);
        if (object == null) {
            final List<Object> assigns = objects.entrySet().stream().filter(e -> Types.isAssignable(type, e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
            if (assigns.size() == 1) {
                object = assigns.get(0);
            } else if (assigns.size() > 1) {
                throw new IllegalArgumentException("Multiple bindings for " + type);
            } else {
                object = injector.get(this, type, access);
            }
            buffer(type, object);
        }
        return object;
    }

    private void buffer(Type type, Object object) {
        if(object != null) {
            Object old = objects.put(type, object);
            if (old != null && old != object) {
                throw new IllegalArgumentException("Multiple bindings for " + type);
            }
        }
    }

    @Override
    public void injectMembers(InjectSpec spec, Object object) throws ReflectiveOperationException {
        buffer(spec.type(), object);
        ObjectFactory.super.injectMembers(spec, object);
    }
}
