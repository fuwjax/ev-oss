package org.echovantage.generic;

import org.echovantage.util.ObjectAssist;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/19/15.
 */
public abstract class AbstractMember<M extends AccessibleObject & Member & AnnotatedElement> extends ObjectAssist.Impl implements GenericMember {
    private final M m;

    public AbstractMember(M m) {
        m.setAccessible(true);
        this.m = m;
    }

    protected M member() {
        return m;
    }

    @Override
    public String name() {
        return m.getName();
    }

    @Override
    public Annotation[] annotations() {
        return m.getAnnotations();
    }

    @Override
    public <A extends Annotation> A[] annotation(Class<A> type) {
        return m.getDeclaredAnnotationsByType(type);
    }

    @Override
    public Type declaringClass() {
        return m.getDeclaringClass();
    }

    @Override
    public MemberAccess access() {
        return MemberAccess.of(m.getModifiers());
    }

    @Override
    public TargetType target() {
        return TargetType.of(m.getModifiers());
    }

    @Override
    public Object[] ids() {
        return GenericMember.ids(this);
    }
}

