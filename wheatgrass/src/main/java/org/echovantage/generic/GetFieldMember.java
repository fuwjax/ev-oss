package org.echovantage.generic;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/19/15.
 */
public class GetFieldMember extends AbstractMember<Field> {
    public GetFieldMember(Field f) {
        super(f);
    }

    @Override
    public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
        assert args.length == 0;
        return source().get(target);
    }

    @Override
    public AnnotatedType[] paramTypes() {
        return new AnnotatedType[0];
    }

    @Override
    public AnnotatedType returnType() {
        return source().getAnnotatedType();
    }

    @Override
    public MemberType type() {
        return MemberType.FIELD_GET;
    }
}
