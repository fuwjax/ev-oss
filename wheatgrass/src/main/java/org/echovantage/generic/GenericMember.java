package org.echovantage.generic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.function.Predicate;

import static java.util.Comparator.*;
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

    Annotation[] annotations();

    <A extends Annotation> A[] annotation(Class<A> type);

    Type[] paramTypes();

    Type returnType();

    Type declaringClass();

    MemberAccess access();

    MemberType type();

    TargetType target();

    static Object[] ids(GenericMember m) {
        return new Object[]{GenericMember.class, m.type() == METHOD ? null : m.declaringClass(), m.type(), m.name(), m.paramTypes()};
    }

    Comparator<GenericMember> COMPARATOR = comparing(GenericMember::type, comparingInt(MemberType::ordinal))
            .thenComparing(nullsLast(comparing(GenericMember::declaringClass, TYPE_COMPARATOR)))
            .thenComparing(GenericMember::name)
            .thenComparing(GenericMember::paramTypes, comparingArray(TYPE_COMPARATOR));
}
