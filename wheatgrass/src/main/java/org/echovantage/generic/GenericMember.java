package org.echovantage.generic;

import org.echovantage.inject.Injector;

import java.lang.annotation.Annotation;

/**
 * Created by fuwjax on 2/18/15.
 */
public interface GenericMember {
    enum MemberAccess{
        PUBLIC, PRIVATE, PROTECTED, PACKAGE
    }

    enum MemberType{
        CONSTRUCTOR, METHOD, FIELD
    }

    Object invoke(Injector source);

    Object invoke(Injector source, Object target);

    Annotation[] getAnnotations();

    Generic[] paramTypes();

    Generic returnType();

    boolean isStatic();

    MemberAccess access();

    MemberType type();
}
