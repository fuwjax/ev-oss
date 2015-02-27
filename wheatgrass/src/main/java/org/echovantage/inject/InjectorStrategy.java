package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;

import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/20/15.
 */
public interface InjectorStrategy {
    Binding bindingFor(BindConstraint constraint);
}
