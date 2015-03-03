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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.nullsLast;
import static org.echovantage.generic.GenericMember.MemberType.METHOD;
import static org.echovantage.util.Arrays2.comparingArray;
import static org.echovantage.util.Types.TYPE_COMPARATOR;

/**
 * Created by fuwjax on 2/18/15.
 */
public interface GenericMember {
    enum MemberAccess implements Predicate<GenericMember> {
        PUBLIC, PROTECTED, PACKAGE, PRIVATE;

        @Override
        public boolean test(GenericMember member) {
            return member.access().ordinal() <= ordinal();
        }

        public static MemberAccess of(int modifiers) {
            if (Modifier.isPublic(modifiers)) {
                return PUBLIC;
            }
            if (Modifier.isProtected(modifiers)) {
                return PROTECTED;
            }
            if (Modifier.isPrivate(modifiers)) {
                return PRIVATE;
            }
            return PACKAGE;
        }
    }

    enum MemberType implements Predicate<GenericMember> {
        CONSTRUCTOR, FIELD_SET, METHOD, FIELD_GET;

        @Override
        public boolean test(GenericMember member) {
            return member.type() == this;
        }
    }

    enum TargetType implements Predicate<GenericMember> {
        TYPE, INSTANCE;

        @Override
        public boolean test(GenericMember member) {
            return member.target() == this;
        }

        public static TargetType of(int modifiers) {
            return Modifier.isStatic(modifiers) ? TYPE : INSTANCE;
        }
    }

    Object invoke(Object target, Object... args) throws ReflectiveOperationException;

    String name();

    AnnotatedElement source();

    AnnotatedDeclaration[] paramTypes();

    AnnotatedDeclaration returnType();

    AnnotatedDeclaration declaringClass();

    MemberAccess access();

    MemberType type();

    TargetType target();

    static Object[] ids(GenericMember m) {
        return new Object[]{GenericMember.class, m.type() == METHOD ? null : m.declaringClass(), m.type(), m.name(), m.paramTypes()};
    }

    Comparator<GenericMember> COMPARATOR = comparing(GenericMember::type, comparingInt(MemberType::ordinal))
            .thenComparing(nullsLast(comparing(GenericMember::declaringClass, comparing(AnnotatedDeclaration::type, TYPE_COMPARATOR))))
            .thenComparing(GenericMember::name)
            .thenComparing(GenericMember::paramTypes, comparingArray(comparing(AnnotatedDeclaration::type, TYPE_COMPARATOR)));
}
