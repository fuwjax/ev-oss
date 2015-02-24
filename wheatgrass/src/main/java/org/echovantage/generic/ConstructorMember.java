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
