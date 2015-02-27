package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.util.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Predicate;

import static org.echovantage.generic.GenericMember.MemberAccess.PUBLIC;

/**
 * Created by fuwjax on 2/26/15.
 */
public class BindConstraint implements Predicate<GenericMember> {
    private final Type type;
    private final MemberAccess access;
    private final Annotation[] annotations;

    BindConstraint(Type type, MemberAccess access, Annotation... annotations) {
        this.type = type;
        this.access = access;
        this.annotations = annotations;
    }

    public BindConstraint(Type type, Annotation... annotations) {
        this(type, PUBLIC, annotations);
    }

    public Type type() {
        return type;
    }

    @Override
    public boolean test(GenericMember member) {
        if (!Types.isAssignable(member.returnType(), type)) {
            return false;
        }
        if (!access.test(member)) {
            return false;
        }
        for (Annotation annotation : annotations) {
            Annotation[] a = member.annotation(annotation.getClass());
            if (!Arrays.asList(a).contains(annotation)) {
                return false;
            }
        }
        return true;
    }
}
