package org.echovantage.inject;

import org.echovantage.generic.GenericMember.MemberAccess;

import java.lang.reflect.Type;

public class SpawnStrategy implements InjectorStrategy {
    @Override
    public Binding bindingFor(Type type, MemberAccess access) {
        try {
            return InjectSpec.of(type);
        }catch(ReflectiveOperationException e){
            return null;
        }
    }
}
