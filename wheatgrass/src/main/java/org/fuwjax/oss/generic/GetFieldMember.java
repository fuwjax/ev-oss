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

import java.lang.reflect.Field;

/**
 * Created by fuwjax on 2/19/15.
 */
public class GetFieldMember extends AbstractMember<Field> {
    public GetFieldMember(Field f) {
        super(f);
    }

    @Override
    public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
        assert args.length == 0;
        return source().get(target);
    }

    @Override
    public AnnotatedDeclaration[] paramTypes() {
        return new AnnotatedDeclaration[0];
    }

    @Override
    public AnnotatedDeclaration returnType() {
        return new AnnotatedDeclaration(source().getGenericType(), source());
    }

    @Override
    public MemberType type() {
        return MemberType.FIELD_GET;
    }
}
