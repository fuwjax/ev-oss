package org.echovantage.inject;

import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.util.ObjectAssist;
import org.echovantage.util.Types;
import sun.security.util.Cache;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.generic.GenericMember.MemberAccess.PUBLIC;

/**
 * Created by fuwjax on 2/26/15.
 */
public class BindConstraint extends ObjectAssist.Base implements Predicate<GenericMember> {
    private final Type type;
    private final MemberAccess access;
    private final Set<Annotation> annotations;

    private BindConstraint(Type type, MemberAccess access, Annotation... annotations) {
        super(type, annotations); // access intentionally omitted
        this.type = type;
        this.access = access;
        this.annotations = Arrays.asList(annotations).stream().filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).collect(toSet());
    }

    BindConstraint(Type type, Annotation... annotations) {
        this(type, PUBLIC, annotations);
    }

    BindConstraint(AnnotatedType type){
        this(type.getType(), PROTECTED, type.getAnnotations());
    }

    public Type type() {
        return type;
    }

    @Override
    public boolean test(GenericMember member) {
        if (!Types.isAssignable(member.returnType().getType(), type)) {
            return false;
        }
        if (!access.test(member)) {
            return false;
        }
        for (Annotation annotation : annotations) {
            Annotation[] a = member.source().getAnnotationsByType(annotation.annotationType());
            if (!Arrays.asList(a).contains(annotation)) {
                return false;
            }
        }
        return true;
    }
}
