package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;

import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/20/15.
 */
public interface InjectorStrategy {
    Binding bindingFor(Type type, MemberAccess access);

    default Object get(ObjectFactory source, Type type, MemberAccess access) throws ReflectiveOperationException {
        Binding binding = bindingFor(type, access);
        if(binding == null){
            throw new ReflectiveOperationException("No binding for "+type);
        }
        try {
            return binding.get(source);
        }catch(ReflectiveOperationException e){
            throw new ReflectiveOperationException("Could not fetch "+type, e);
        }
    }
}
