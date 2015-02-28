package org.echovantage.generic;

import org.echovantage.inject.Injector;
import org.echovantage.util.Types;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static org.echovantage.util.Types.annotatedType;

/**
 * Created by fuwjax on 2/19/15.
 */
public class SetFieldMember extends AbstractMember<Field> {

    public SetFieldMember(Field f) {
        super(f);
    }

    @Override
    public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
        assert args.length == 1;
        source().set(target, args[0]);
        return null;
    }

    @Override
    public AnnotatedType[] paramTypes() {
        return new AnnotatedType[]{source().getAnnotatedType()};
    }

    @Override
    public AnnotatedType returnType() {
        return annotatedType(void.class);
    }

    @Override
    public MemberType type() {
        return MemberType.FIELD_SET;
    }
}
