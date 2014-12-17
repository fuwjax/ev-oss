package org.echovantage.util;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class Elements {
	public static boolean hasConstructor(final TypeElement type, final ExecutableElement signature) {
		for(final Element e : type.getEnclosedElements()) {
			if(e.getKind().equals(ElementKind.CONSTRUCTOR)) {
				final ExecutableElement constructor = (ExecutableElement)e;
				if(isCallable(constructor.getParameters(), signature.getParameters())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isCallable(final List<? extends VariableElement> cParams, final List<? extends VariableElement> mParams) {
		if(cParams.size() != mParams.size()) {
			return false;
		}
		for(int i = 0; i < cParams.size(); i++) {
			if(!isAssignable(typeOf(cParams.get(i).asType()), typeOf(mParams.get(i).asType()))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAssignable(final TypeElement variable, final TypeElement expression) {
		if(expression == null) {
			return false;
		}
		if(variable.equals(expression)) {
			return true;
		}
		if(isAssignable(variable, typeOf(expression.getSuperclass()))) {
			return true;
		}
		for(final TypeMirror i : expression.getInterfaces()) {
			if(isAssignable(variable, typeOf(i))) {
				return true;
			}
		}
		return false;
	}

	public static TypeElement typeOf(final TypeMirror mirror) {
		return mirror.getKind() == TypeKind.NONE ? null : mirror.getKind() == TypeKind.VOID ? null : (TypeElement)((DeclaredType)mirror).asElement();
	}

	public static boolean isService(final TypeElement type) {
		if(!type.getKind().isClass()) {
			return false;
		}
		if(isAbstract(type)) {
			return false;
		}
		if(isNested(type) && !isStatic(type)) {
			return false;
		}
		return hasPublicDefaultConstructor(type);
	}

	public static boolean isStatic(final TypeElement type) {
		return type.getModifiers().contains(Modifier.STATIC);
	}

	public static boolean isNested(final TypeElement type) {
		return !ElementKind.PACKAGE.equals(type.getEnclosingElement().getKind());
	}

	public static boolean isAbstract(final TypeElement type) {
		return type.getModifiers().contains(Modifier.ABSTRACT);
	}

	public static boolean hasPublicDefaultConstructor(final TypeElement type) {
		for(final Element e : type.getEnclosedElements()) {
			if(e.getKind().equals(ElementKind.CONSTRUCTOR) && ((ExecutableElement)e).getParameters().isEmpty()) {
				return isPublic(type);
			}
		}
		return false;
	}

	public static boolean isPublic(final TypeElement type) {
		return type.getModifiers().contains(Modifier.PUBLIC);
	}

	public static ExecutableElement functionalMethod(final TypeElement contract) {
		if(!isInterface(contract) && !hasPublicDefaultConstructor(contract)) {
			throw new IllegalArgumentException(String.format("Service contract %s must be an interface or abstract class with a default constructor", contract));
		}
		ExecutableElement m = null;
		for(final Element e : contract.getEnclosedElements()) {
			if(e.getKind() == ElementKind.METHOD) {
				if(e.getModifiers().contains(Modifier.ABSTRACT)) {
					if(m != null) {
						throw new IllegalArgumentException(String.format("Service contract %s must contain exactly one abstract method", contract));
					}
					m = (ExecutableElement)e;
				}
			}
		}
		return m;
	}

	public static boolean isInterface(final TypeElement contract) {
		return contract.getKind() == ElementKind.INTERFACE;
	}

	public static PackageElement packageOf(final Element service) {
		final Element e = service.getEnclosingElement();
		if(e == null) {
			return null;
		}
		return e.getKind() == ElementKind.PACKAGE ? (PackageElement)e : packageOf(e);
	}

	public static String invokeArgs(final ExecutableElement method) {
		return method.getParameters().stream().map(Object::toString).collect(Collectors.joining(", "));
	}

	public static String throwsDecl(final ExecutableElement method) {
		return method.getThrownTypes().isEmpty() ? "" : method.getThrownTypes().stream().map(Object::toString).collect(Collectors.joining(", ", " throws ", ""));
	}

	public static String parameterDecl(final ExecutableElement method) {
		return method.getParameters().stream().map(p -> p.asType() + " " + p).collect(Collectors.joining(", "));
	}

	public static boolean areEqual(final TypeElement annotation, final Class<?> type) {
		return type.getCanonicalName().equals(annotation.getQualifiedName().toString());
	}
}
