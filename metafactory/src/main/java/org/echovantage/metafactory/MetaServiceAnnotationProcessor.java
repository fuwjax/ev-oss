/*
 * The MIT License Copyright (c) 2009-, Kohsuke Kawaguchi Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.echovantage.metafactory;

import static java.util.Collections.singleton;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

/**
 * @author Fuwjax
 */
public class MetaServiceAnnotationProcessor extends AbstractProcessor {
	private final Map<CharSequence, MetaFactoryServices> metas = new HashMap<>();

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return singleton(MetaService.class.getName());
	}

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		for(final TypeElement annotation : annotations) {
			try {
				for(final Element service : roundEnv.getElementsAnnotatedWith(annotation)) {
					if(!isService((TypeElement)service)) {
						error(service, "Not a valid service");
						continue;
					}
					final MetaService meta = service.getAnnotation(MetaService.class);
					TypeElement contract = typeOf(meta::value);
					if(contract == null) {
						contract = inferContract((TypeElement)service);
						if(contract == null) {
							continue;
						}
					}
					MetaFactoryServices factory = metas.get(contract.getQualifiedName());
					if(factory == null) {
						factory = new MetaFactoryServices(contract);
						metas.put(contract.getQualifiedName(), factory);
					}
					factory.process((TypeElement)service);
				}
			} catch(final IOException e) {
				error(annotation, "IO error while processing annotation: %s", e.getLocalizedMessage());
			} catch(final RuntimeException e) {
				error(annotation, "Unexpected exception: %s: %s", e.getClass(), e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		if(roundEnv.processingOver()) {
			for(final MetaFactoryServices factory : metas.values()) {
				factory.writeMetaInfServices();
			}
		}
		return false;
	}

	private TypeElement inferContract(final TypeElement service) {
		if(service == null) {
			return null;
		}
		final TypeElement superclass = typeOf(service.getSuperclass());
		if("java.lang.Object".equals(superclass.getQualifiedName().toString())) {
			if(service.getInterfaces().size() != 1) {
				error(service, "Needed exactly one intervace on service to infer contract");
			}
			return typeOf(service.getInterfaces().get(0));
		}
		if(!service.getInterfaces().isEmpty()) {
			error(service, "Could not choose between superclass and interface for contract");
		}
		return superclass;
	}

	private class MetaFactoryServices {
		final Set<TypeElement> services = new HashSet<>();
		private final TypeElement contract;

		public MetaFactoryServices(final TypeElement contract) throws IOException {
			this.contract = contract;
			loadExisting();
		}

		private void loadExisting() throws IOException {
			try {
				final FileObject f = getResource(StandardLocation.SOURCE_PATH, "", metaInfServices());
				try(InputStream input = f.openInputStream();
						Reader reader = new InputStreamReader(input, "UTF-8");
						final BufferedReader r = new BufferedReader(reader)) {
					String line;
					while((line = r.readLine()) != null) {
						final TypeElement type = typeOf(line);
						if(isService(type) && isAssignable(contract, type)) {
							services.add(type);
						} else {
							error("Existing service factory %s is not a public concrete class with a default constructor implementing %s, removing it from meta-inf/services", type, contract);
						}
					}
				}
			} catch(final FileNotFoundException x) {
				// doesn't exist
			}
		}

		private String metaInfServices() {
			return "META-INF/services/" + binaryNameOf(contract);
		}

		public void process(final TypeElement service) throws IOException {
			services.add(service);
		}

		public void writeMetaInfServices() {
			log("Writing " + metaInfServices());
			try {
				final FileObject metainfServices = createResource(StandardLocation.CLASS_OUTPUT, "", metaInfServices(), services.toArray(new Element[services.size()]));
				try(Writer writer = metainfServices.openWriter();
						PrintWriter pw = new PrintWriter(writer)) {
					for(final TypeElement value : services) {
						pw.println(binaryNameOf(value));
					}
				}
			} catch(final IOException e) {
				error("IO error while writing to meta-inf/services: %s", e.getLocalizedMessage());
			} catch(final RuntimeException e) {
				error("Unexpected exception: %s: %s", e.getClass(), e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		private void error(final String pattern, final Object... args) {
			MetaServiceAnnotationProcessor.this.error(contract, pattern, args);
		}
	}

	static boolean isInterface(final TypeElement contract) {
		return contract.getKind() == ElementKind.INTERFACE;
	}

	static boolean hasConstructor(final TypeElement type, final ExecutableElement signature) {
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

	static boolean isCallable(final List<? extends VariableElement> cParams, final List<? extends VariableElement> mParams) {
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

	PackageElement packageOf(final Element service) {
		final Element e = service.getEnclosingElement();
		if(e == null) {
			return null;
		}
		return e.getKind() == ElementKind.PACKAGE ? (PackageElement)e : packageOf(e);
	}

	FileObject createResource(final JavaFileManager.Location location,
			final CharSequence pkg,
			final CharSequence relativeName,
			final Element... originatingElements) throws IOException {
		return processingEnv.getFiler().createResource(location, pkg, relativeName, originatingElements);
	}

	FileObject getResource(final Location location,
			final CharSequence pkg,
			final CharSequence relativeName) throws IOException {
		return processingEnv.getFiler().getResource(location, pkg, relativeName);
	}

	String binaryNameOf(final TypeElement elm) {
		return processingEnv.getElementUtils().getBinaryName(elm).toString();
	}

	TypeElement typeOf(final String name) {
		return processingEnv.getElementUtils().getTypeElement(name);
	}

	TypeElement typeOf(final Supplier<Class<?>> value) {
		try {
			final Class<?> cls = value.get();
			return typeOf(cls.getName());
		} catch(final MirroredTypeException e) {
			return typeOf(e.getTypeMirror());
		}
	}

	static boolean isAssignable(final TypeElement variable, final TypeElement expression) {
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

	static TypeElement typeOf(final TypeMirror superclass) {
		return superclass.getKind() == TypeKind.NONE ? null : superclass.getKind() == TypeKind.VOID ? null : (TypeElement)((DeclaredType)superclass).asElement();
	}

	static boolean isService(final TypeElement type) {
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

	static boolean isStatic(final Element type) {
		return type.getModifiers().contains(Modifier.STATIC);
	}

	static boolean isNested(final Element type) {
		return !ElementKind.PACKAGE.equals(type.getEnclosingElement().getKind());
	}

	static boolean isAbstract(final Element type) {
		return type.getModifiers().contains(Modifier.ABSTRACT);
	}

	static boolean hasPublicDefaultConstructor(final TypeElement type) {
		for(final Element e : type.getEnclosedElements()) {
			if(e.getKind().equals(ElementKind.CONSTRUCTOR) && ((ExecutableElement)e).getParameters().isEmpty()) {
				return isPublic(e) && isPublic(type);
			}
		}
		return false;
	}

	static boolean isPublic(final Element type) {
		return type.getModifiers().contains(Modifier.PUBLIC);
	}

	void log(final String pattern, final Object... args) {
		processingEnv.getMessager().printMessage(Kind.NOTE, String.format(pattern, args));
	}

	void error(final Element source, final String pattern, final Object... args) {
		processingEnv.getMessager().printMessage(Kind.ERROR, String.format(pattern, args), source);
	}
}
