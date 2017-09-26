package org.fuwjax.oss.inject;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.fuwjax.oss.generic.GenericMember.MemberAccess.PROTECTED;
import static org.fuwjax.oss.generic.GenericMember.MemberAccess.PUBLIC;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Qualifier;

import org.fuwjax.oss.generic.AnnotatedDeclaration;
import org.fuwjax.oss.generic.GenericMember;
import org.fuwjax.oss.generic.GenericMember.MemberAccess;
import org.fuwjax.oss.util.ObjectAssist;
import org.fuwjax.oss.util.Types;

public class Declaration extends ObjectAssist.Base implements Predicate<GenericMember> {
	private final String name;
	private final Type type;
	private final MemberAccess access;
	private final Set<Annotation> annotations;

	private Declaration(String name, Type type, MemberAccess access, Annotation... annotations) {
		this(name, access, type, asList(annotations).stream()
				.filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).collect(toSet()));
	}

	private Declaration(String name, MemberAccess access, Type type, Set<Annotation> annotations) {
		super(type, annotations); // access intentionally omitted
		this.name = name;
		this.access = access;
		this.type = type;
		this.annotations = annotations;
	}

	Declaration(String name, Type type, Annotation... annotations) {
		this(name, type, PUBLIC, annotations);
	}

	Declaration(String name, AnnotatedDeclaration type) {
		this(name, type.type(), PROTECTED, type.element().getAnnotations());
	}

	public Type type() {
		return type;
	}

	@Override
	public boolean test(GenericMember member) {
		if(member.name() != null && name != null && !name.equals(member.name())) {
			return false;
		}
		if (!Types.isAssignable(member.returnType().type(), type)) {
			return false;
		}
		if (!access.test(member)) {
			return false;
		}
		for (Annotation annotation : annotations) {
			Annotation[] a = member.source().getAnnotationsByType(annotation.annotationType());
			if (!asList(a).contains(annotation)) {
				return false;
			}
		}
		return true;
	}
}
