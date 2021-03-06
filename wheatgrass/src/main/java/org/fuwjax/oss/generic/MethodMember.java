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
package org.fuwjax.oss.generic;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/19/15.
 */
public class MethodMember extends AbstractMember<Method> {

    public MethodMember(Method m) {
        super(m);
    }

    @Override
    public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
        return source().invoke(target, args);
    }

    @Override
    public AnnotatedDeclaration[] paramTypes() {
        return AnnotatedDeclaration.of(source().getGenericParameterTypes(), source().getParameterAnnotations());
    }

    @Override
    public AnnotatedDeclaration returnType() {
        return new AnnotatedDeclaration(source().getGenericReturnType(), source());
    }

    @Override
    public MemberType type() {
        return MemberType.METHOD;
    }
}
