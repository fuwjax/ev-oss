package org.echovantage.generic;

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
        return member().invoke(target, args);
    }

    @Override
    public Type[] paramTypes() {
        return member().getGenericParameterTypes();
    }

    @Override
    public Type returnType() {
        return member().getGenericReturnType();
    }

    @Override
    public MemberType type() {
        return MemberType.METHOD;
    }
}
