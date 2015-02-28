package org.echovantage.generic;

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
    public AnnotatedType[] paramTypes() {
        return source().getAnnotatedParameterTypes();
    }

    @Override
    public AnnotatedType returnType() {
        return source().getAnnotatedReturnType();
    }

    @Override
    public MemberType type() {
        return MemberType.METHOD;
    }
}
