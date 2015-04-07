/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.echovantage.inject;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.echovantage.generic.GenericMember.MemberAccess.PROTECTED;
import static org.echovantage.generic.GenericMember.MemberAccess.PUBLIC;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Qualifier;

import org.echovantage.generic.AnnotatedDeclaration;
import org.echovantage.generic.GenericMember;
import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.util.ObjectAssist;
import org.echovantage.util.Types;

/**
 * Created by fuwjax on 2/26/15.
 */
public class BindConstraint extends ObjectAssist.Base implements Predicate<GenericMember> {
	private final Type type;
	private final MemberAccess access;
	private final Set<Annotation> annotations;

	private BindConstraint(final Type type, final MemberAccess access, final Annotation... annotations) {
		this(access, type, asList(annotations).stream().filter(BindConstraint::isQualified).collect(toSet()));
	}

	private BindConstraint(final MemberAccess access, final Type type, final Set<Annotation> annotations) {
		super(type, annotations); // access intentionally omitted
		this.access = access;
		this.type = type;
		this.annotations = annotations;
	}

	private static boolean isQualified(final Annotation annotation) {
		return annotation.annotationType().isAnnotationPresent(Qualifier.class);
	}

	BindConstraint(final Type type, final Annotation... annotations) {
		this(type, PUBLIC, annotations);
	}

	BindConstraint(final AnnotatedDeclaration type) {
		this(type.type(), PROTECTED, type.element().getAnnotations());
	}

	public Type type() {
		return type;
	}

	@Override
	public boolean test(final GenericMember member) {
		if(!Types.isAssignable(member.returnType().type(), type)) {
			return false;
		}
		if(!access.test(member)) {
			return false;
		}
		for(final Annotation annotation : annotations) {
			final Annotation[] a = member.source().getAnnotationsByType(annotation.annotationType());
			if(!asList(a).contains(annotation)) {
				return false;
			}
		}
		return true;
	}
}
