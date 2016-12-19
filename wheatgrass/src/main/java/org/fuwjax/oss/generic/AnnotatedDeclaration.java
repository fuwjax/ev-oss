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
package org.fuwjax.oss.generic;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.fuwjax.oss.util.Arrays2;
import org.fuwjax.oss.util.Types;

/**
 * Created by fuwjax on 3/3/15.
 */
public class AnnotatedDeclaration {
    private final Type type;
    private final AnnotatedElement element;

    public AnnotatedDeclaration(Class<?> type) {
        this(type, type);
    }

    public AnnotatedDeclaration(Type type, AnnotatedElement element) {
        this.type = type;
        this.element = element;
    }

    public AnnotatedDeclaration(Type type, Annotation... annotations){
        this(type, annotatedElement(annotations));
    }

    private static AnnotatedElement annotatedElement(Annotation[] annotations) {
        Map<Class<?>, Annotation> map = Arrays.asList(annotations).stream().collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
        return new AnnotatedElement() {
            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
                return annotationClass.cast(map.get(annotationClass));
            }

            @Override
            public Annotation[] getAnnotations() {
                return annotations;
            }

            @Override
            public Annotation[] getDeclaredAnnotations() {
                return getAnnotations();
            }
        };
 	}

	public static AnnotatedDeclaration[] of(Type[] types, Annotation[][] annotations) {
        return Arrays2.zip(types, annotations, new AnnotatedDeclaration[types.length], AnnotatedDeclaration::new);
    }

    public AnnotatedElement element() {
        return element;
    }

    public AnnotatedDeclaration subst(ParameterizedType mapping) {
        return new AnnotatedDeclaration(Types.subst(type, mapping), element);
    }
    
//    public AnnotatedDeclaration subst(TypeVariable<?>[] params, Type[] args) {
//    	return new AnnotatedDeclaration(Types.subst(type, params, args), element);
//    }

    public Type type() {
        return type;
    }
}
