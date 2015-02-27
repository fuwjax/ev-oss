package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.generic.TypeTemplate;
import org.echovantage.util.Arrays2;
import org.echovantage.util.RunWrapException;
import org.echovantage.util.Streams;

import java.lang.reflect.Type;

import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.generic.GenericMember.MemberAccess.PUBLIC;
import static org.echovantage.util.function.Functions.function;

/**
 * Created by fuwjax on 2/20/15.
 */
public interface ObjectFactory {
    Object get(BindConstraint constraint) throws ReflectiveOperationException;

    void inject (BindConstraint constraint, Object target) throws ReflectiveOperationException;

    default <T> T inject(final T object) throws ReflectiveOperationException {
        inject(new BindConstraint(object.getClass()), object);
        return object;
    }

    default <T> T inject(TypeTemplate<T> type, final T object) throws ReflectiveOperationException {
        assert object.getClass().equals(type.getRawType());
        inject(new BindConstraint(type), object);
        return object;
    }

    default <T> T get(final Class<T> type) throws ReflectiveOperationException {
        return (T) get(new BindConstraint(type, PUBLIC));
    }

    default <T> T get(final TypeTemplate<T> type) throws ReflectiveOperationException {
        return (T) get(new BindConstraint(type, PUBLIC));
    }
}
