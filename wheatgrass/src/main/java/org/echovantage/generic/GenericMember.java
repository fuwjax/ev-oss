package org.echovantage.generic;

import org.echovantage.inject.Injector;
import org.echovantage.util.Arrays2;
import org.echovantage.util.RunWrapException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.Predicate;

import static org.echovantage.util.function.Functions.function;

/**
 * Created by fuwjax on 2/18/15.
 */
public interface GenericMember {
    enum MemberAccess implements Predicate<GenericMember> {
        PUBLIC, PROTECTED, PACKAGE, PRIVATE;

        @Override
        public boolean test(GenericMember member) {
            return member.access().ordinal() < ordinal();
        }

        public static MemberAccess of(int modifiers) {
            if (Modifier.isPublic(modifiers)) {
                return PUBLIC;
            }
            if(Modifier.isProtected(modifiers)){
                return PROTECTED;
            }
            if(Modifier.isPrivate(modifiers)){
                return PRIVATE;
            }
            return PACKAGE;
        }
    }

    enum MemberType implements Predicate<GenericMember>{
        CONSTRUCTOR, METHOD, FIELD_GET, FIELD_SET;

        @Override
        public boolean test(GenericMember member) {
            return member.type() == this;
        }
    }

    enum TargetType implements Predicate<GenericMember>{
        TYPE, INSTANCE;

        @Override
        public boolean test(GenericMember member) {
            return member.target() == this;
        }

        public static TargetType of(int modifiers) {
            return Modifier.isStatic(modifiers) ? TYPE : INSTANCE;
        }
    }

    default Object inject(Injector source) {
        return inject(source, null);
    }

    default Object inject(Injector source, Object target) {
        return source.invoke(this, target);
    }

    Object invoke(Object target, Object... args) throws ReflectiveOperationException;

    String name();

    Annotation[] annotations();

    Type[] paramTypes();

    Type returnType();

    MemberAccess access();

    MemberType type();

    TargetType target();
}
