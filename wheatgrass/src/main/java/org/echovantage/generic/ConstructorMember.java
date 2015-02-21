package org.echovantage.generic;

import org.echovantage.inject.Injector;
import org.echovantage.util.Arrays2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Objects;

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
