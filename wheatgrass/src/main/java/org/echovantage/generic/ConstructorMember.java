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
package org.echovantage.generic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/19/15.
 */
public class ConstructorMember extends AbstractMember<Constructor<?>> {

    public ConstructorMember(Constructor<?> c) {
        super(c);
    }

    @Override
    public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
        return member().newInstance(args);
    }

    @Override
    public String name() {
        return "new";
    }

    @Override
    public Type[] paramTypes() {
        return member().getGenericParameterTypes();
    }

    @Override
    public Type returnType() {
        return member().getDeclaringClass();
    }

    @Override
    public MemberType type() {
        return MemberType.CONSTRUCTOR;
    }

    @Override
    public TargetType target() {
        return TargetType.TYPE;
    }
}
