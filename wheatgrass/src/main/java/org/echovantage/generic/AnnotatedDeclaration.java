package org.echovantage.generic;

import org.echovantage.util.Arrays2;
import org.echovantage.util.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
        this(type, Types.annotatedElement(annotations));
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

    public Type type() {
        return type;
    }
}
