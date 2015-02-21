package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;

import java.lang.reflect.Type;

public class SpawnInjector implements InjectorStrategy {
    @Override
    public Binding bindingFor(Type type, MemberAccess access) {
        return InjectSpec.get(type);
    }
}
