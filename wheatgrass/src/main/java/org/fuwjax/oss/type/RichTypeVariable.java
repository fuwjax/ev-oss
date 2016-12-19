package org.fuwjax.oss.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.fuwjax.oss.util.Arrays2;

public class RichTypeVariable<D extends GenericDeclaration> implements TypeVariable<D> {
	private AnnotatedType[] bounds;
	private D source;
	private String name;
	
	private RichTypeVariable(D source, String name, AnnotatedType... bounds) {
		this.source = source;
		this.name = name;
		this.bounds = bounds;
	}
	
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return source.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return source.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return source.getDeclaredAnnotations();
	}

	@Override
	public Type[] getBounds() {
		return Arrays2.transform(bounds, new Type[bounds.length], AnnotatedType::getType);
	}

	@Override
	public D getGenericDeclaration() {
		return source;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		return bounds;
	}
}
