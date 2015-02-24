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

import org.echovantage.util.ObjectAssist;
import org.echovantage.util.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/19/15.
 */
public class ResolvedMember extends ObjectAssist.Impl implements GenericMember {
    private final GenericMember member;
    private final Type type;
    private final Type[] params;
    private final Type returns;

    public ResolvedMember(GenericMember member, ParameterizedType type) {
        this.member = member;
        this.type = type;
        params = Types.subst(member.paramTypes(), type);
        returns = Types.subst(member.returnType(), type);
    }

    @Override
    public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
        return member.invoke(target, args);
    }

    @Override
    public String name() {
        return member.name();
    }

    @Override
    public Annotation[] annotations() {
        return member.annotations();
    }

    @Override
    public <A extends Annotation> A[] annotation(Class<A> type) {
        return member.annotation(type);
    }

    @Override
    public Type[] paramTypes() {
        return params;
    }

    @Override
    public Type returnType() {
        return returns;
    }

    @Override
    public Type declaringClass() {
        return type;
    }

    @Override
    public MemberAccess access() {
        return member.access();
    }

    @Override
    public MemberType type() {
        return member.type();
    }

    @Override
    public TargetType target() {
        return member.target();
    }

    @Override
    public Object[] ids() {
        return GenericMember.ids(this);
    }
}
