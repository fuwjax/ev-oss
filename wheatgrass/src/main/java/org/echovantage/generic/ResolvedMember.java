package org.echovantage.generic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/19/15.
 */
public class ResolvedMember implements GenericMember {
    private final GenericMember member;
    private final Type[] params;
    private final Type returns;

    public ResolvedMember(GenericMember member, Type[] params, Type returns) {
        this.member = member;
        this.params = params;
        this.returns = returns;
    }

    @Override
    public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
        return member.invoke(target, args);
    }

    @Override
    public String name() {
        return member.name();
    }

    @Override
    public Annotation[] annotations() {
        return member.annotations();
    }

    @Override
    public Type[] paramTypes() {
        return params;
    }

    @Override
    public Type returnType() {
        return returns;
    }

    @Override
    public MemberAccess access() {
        return member.access();
    }

    @Override
    public MemberType type() {
        return member.type();
    }

    @Override
    public TargetType target() {
        return member.target();
    }
}
