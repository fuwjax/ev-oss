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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.generic.GenericMember.MemberAccess.PUBLIC;
import static org.echovantage.util.function.Functions.function;

/**
 * Created by fuwjax on 2/20/15.
 */
public interface ObjectFactory {
    Object get(BindConstraint constraint) throws ReflectiveOperationException;

    void inject (BindConstraint constraint, Object target) throws ReflectiveOperationException;

    default <T> T inject(final T object) throws ReflectiveOperationException {
        inject(new BindConstraint(object.getClass()), object);
        return object;
    }

    default <T> T inject(TypeTemplate<T> type, final T object) throws ReflectiveOperationException {
        assert object.getClass().equals(type.getRawType());
        inject(new BindConstraint(type), object);
        return object;
    }

    default <T> T get(final Class<T> type, Annotation... annotations) throws ReflectiveOperationException {
        return (T) get(new BindConstraint(type, annotations));
    }

    default <T> T get(final TypeTemplate<T> type, Annotation... annotations) throws ReflectiveOperationException {
        return (T) get(new BindConstraint(type, annotations));
    }
}
