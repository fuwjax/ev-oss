package org.echovantage.generic;

import org.echovantage.inject.Injector;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

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
        member().set(target, args[0]);
        return null;
    }

    @Override
    public Type[] paramTypes() {
        return new Type[]{member().getGenericType()};
    }

    @Override
    public Type returnType() {
        return void.class;
    }

    @Override
    public MemberType type() {
        return MemberType.FIELD_SET;
    }
}
