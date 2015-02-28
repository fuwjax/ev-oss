package org.echovantage.generic;

import java.lang.reflect.AnnotatedType;
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
        return source().newInstance(args);
    }

    @Override
    public String name() {
        return "new";
    }

    @Override
    public AnnotatedType[] paramTypes() {
        return source().getAnnotatedParameterTypes();
    }

    @Override
    public AnnotatedType returnType() {
        return source().getAnnotatedReturnType();
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
