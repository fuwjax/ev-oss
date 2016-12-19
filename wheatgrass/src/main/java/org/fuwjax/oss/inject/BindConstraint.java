/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
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
package org.fuwjax.oss.inject;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.fuwjax.oss.generic.GenericMember.MemberAccess.PROTECTED;
import static org.fuwjax.oss.generic.GenericMember.MemberAccess.PUBLIC;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Qualifier;

import org.fuwjax.oss.generic.AnnotatedDeclaration;
import org.fuwjax.oss.generic.GenericMember;
import org.fuwjax.oss.generic.GenericMember.MemberAccess;
import org.fuwjax.oss.util.Types;
import org.fuwjax.oss.util.ValueObject;

/**
 * The set of bindable properties of an {@link Injector} operation. An {@link Injector} may only select a {@link Binding}
 * for a {@link GenericMember} based on these properties, as determined by the Predicate facet.
 *
 */
public final class BindConstraint extends ValueObject {
    private final Type type;
    private final MemberAccess access;
    private final Set<Annotation> annotations;

    private BindConstraint(Type type, MemberAccess access, Annotation... annotations) {
        this(access, type, asList(annotations).stream().filter(BindConstraint::isQualified).collect(toSet()));
    }

    private BindConstraint(MemberAccess access, Type type, Set<Annotation> annotations) {
    	identify(BindConstraint.class, type, annotations); // access intentionally omitted from identity
        this.access = access;
        this.type = type;
        this.annotations = annotations;
    }

    private static boolean isQualified(Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(Qualifier.class);
    }

    BindConstraint(Type type, Annotation... annotations) {
        this(type, PUBLIC, annotations);
    }

    BindConstraint(AnnotatedDeclaration type){
        this(type.type(), PROTECTED, type.element().getAnnotations());
    }

    /**
     * The bindable type.
     * @return the bindable type
     */
    public Type type() {
        return type;
    }
    
    /**
     * The bindable access level.
     * @return the bindable access level
     */
    public MemberAccess access() {
    	return access;
    }
    
    /**
     * The bindable annotations.
     * @return the bindable annotations.
     */
    public Set<Annotation> annotations(){
    	return Collections.unmodifiableSet(annotations);
    }
    
    public GenericMember coerce(GenericMember member) {
        if (!access.test(member)) {
            return null;
        }
        for (Annotation annotation : annotations) {
            Annotation[] a = member.source().getAnnotationsByType(annotation.annotationType());
            if (!asList(a).contains(annotation)) {
                return null;
            }
        }
        return member.coerceReturn(type);
    }

	public Binding defaultBinding() {
		if(type instanceof ParameterizedType && Class.class.equals(Types.rawType(type))){
			return scope -> Types.rawType(((ParameterizedType)type).getActualTypeArguments()[0]);
		}
		return scope -> scope.create(this);
	}
}
