package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.util.Arrays2;
import org.echovantage.util.RunWrapException;

import java.lang.reflect.Type;

import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.util.function.Functions.function;

/**
 * Created by fuwjax on 2/20/15.
 */
public interface ObjectFactory {

    Object[] NO_ARGS = new Object[0];

    Object get(Type type, MemberAccess access) throws ReflectiveOperationException;

    default Object invoke(Object target, GenericMember member) throws ReflectiveOperationException {
        try {
            Object[] args = Arrays2.transform(member.paramTypes(), NO_ARGS, function(t -> get(t, PROTECTED)));
            return member.invoke(target, args);
        }catch(RunWrapException e){
            throw e.throwIf(ReflectiveOperationException.class);
        }
    }
}
