package org.fuwjax.oss.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

public class RichAnnotatedType implements AnnotatedType{
	private Type type;
	private AnnotatedElement element;
	
	public RichAnnotatedType(Type type, AnnotatedElement element) {
		this.type = type;
		this.element = element;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return element.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return element.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return element.getDeclaredAnnotations();
	}
}
