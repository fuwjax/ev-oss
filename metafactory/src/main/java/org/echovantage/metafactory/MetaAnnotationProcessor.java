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

import static org.echovantage.util.Elements.isAssignable;
import static org.echovantage.util.Elements.isService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.echovantage.metafactory.MetaInfContext.MetaInfContract;
import org.echovantage.util.Elements;

import com.sun.tools.javac.util.ServiceLoader;

/**
 * @author Fuwjax
 */
@SupportedAnnotationTypes("*")
public class MetaAnnotationProcessor extends AbstractProcessor {
	private final Map<String, MetaContract> metas = new HashMap<>();
	private final ServiceLoader<AnnotationProcessor> processors = ServiceLoader.load(AnnotationProcessor.class);

	private class MetaContext implements MetaInfContext {
		private final RoundEnvironment roundEnv;

		public MetaContext(final RoundEnvironment roundEnv) {
			this.roundEnv = roundEnv;
		}

		@Override
		public TypeElement typeOf(final Supplier<Class<?>> meta) {
			try {
				return getTypeElement(meta.get().getName());
			} catch(final MirroredTypeException e) {
				return Elements.typeOf(e.getTypeMirror());
			}
		}

		@Override
		public Collection<? extends Element> getElementsAnnotatedWith(final TypeElement annotation) {
			return roundEnv.getElementsAnnotatedWith(annotation);
		}

		@Override
		public MetaContract getContract(final TypeElement contract) {
			MetaContract factory = metas.get(contract.getQualifiedName().toString());
			if(factory == null) {
				factory = new MetaContract(contract);
				metas.put(contract.getQualifiedName().toString(), factory);
			}
			return factory;
		}

		@Override
		public JavaFileObject createSourceFile(final CharSequence name,
		      final Element... originatingElements) throws IOException {
			return MetaAnnotationProcessor.this.createSourceFile(name, originatingElements);
		}
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		final MetaInfContext context = new MetaContext(roundEnv);
		for(final TypeElement annotation : annotations) {
			try {
				for(final AnnotationProcessor processor : processors) {
					if(processor.process(annotation, context)) {
						break;
					}
				}
			} catch(final IOException e) {
				final Object[] args = { e.getLocalizedMessage() };
				printMessage(Kind.ERROR, String.format("IO error while processing annotation: %s", args), annotation);
			} catch(final RuntimeException e) {
				final Object[] args = { e.getClass(), e.getLocalizedMessage() };
				printMessage(Kind.ERROR, String.format("Unexpected exception: %s: %s", args), annotation);
				e.printStackTrace();
			}
		}
		if(roundEnv.processingOver()) {
			for(final MetaContract factory : metas.values()) {
				writeMetaInfServices(factory);
			}
		}
		return false;
	}

	private void writeMetaInfServices(final MetaContract c) {
		if(c.services.isEmpty()) {
			return;
		}
		final Object[] args = {};
		printMessage(Kind.NOTE, String.format("Writing " + c.metaInfServices(), args));
		try {
			loadExisting(c);
			final FileObject metainfServices = createResource(StandardLocation.CLASS_OUTPUT, "", c.metaInfServices(), c.origins());
			try(Writer writer = metainfServices.openWriter();
			      PrintWriter pw = new PrintWriter(writer)) {
				for(final String value : c.services) {
					pw.println(value);
				}
			}
		} catch(final IOException e) {
			final Object[] args1 = { e.getLocalizedMessage() };
			printMessage(Kind.ERROR, String.format("IO error while writing to meta-inf/services: %s", args1), c.contract);
		} catch(final RuntimeException e) {
			final Object[] args1 = { e.getClass(), e.getLocalizedMessage() };
			printMessage(Kind.ERROR, String.format("Unexpected exception: %s: %s", args1), c.contract);
			e.printStackTrace();
		}
	}

	private void loadExisting(final MetaContract c) throws IOException {
		try {
			final FileObject f = getResource(StandardLocation.SOURCE_PATH, "", c.metaInfServices());
			try(InputStream input = f.openInputStream();
			      Reader reader = new InputStreamReader(input, "UTF-8");
			      final BufferedReader r = new BufferedReader(reader)) {
				String line;
				while((line = r.readLine()) != null) {
					final TypeElement type = getTypeElement(line);
					if(isService(type) && isAssignable(c.contract, type)) {
						c.add(line);
					} else {
						final Object[] args = { type, c.contract };
						printMessage(Kind.ERROR, String.format("Existing service factory %s is not a public concrete class with a default constructor implementing %s, removing it from meta-inf/services", args), c.contract);
					}
				}
			}
		} catch(final FileNotFoundException x) {
			// doesn't exist
		}
	}

	private class MetaContract implements MetaInfContract {
		private final Set<String> services = new HashSet<>();
		private final Set<Element> origins = new HashSet<>();
		final TypeElement contract;

		public MetaContract(final TypeElement contract) {
			this.contract = contract;
		}

		Element[] origins() {
			return origins.toArray(new Element[origins.size()]);
		}

		String metaInfServices() {
			return "META-INF/services/" + getBinaryName(contract);
		}

		@Override
		public boolean watch(final Element origin) {
			return origins.add(origin);
		}

		@Override
		public void add(final String service) {
			services.add(service);
		}

		@Override
		public void add(final TypeElement service) {
			add(getBinaryName(service).toString());
		}
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

	JavaFileObject createSourceFile(final CharSequence name,
			final Element... originatingElements) throws IOException {
		return processingEnv.getFiler().createSourceFile(name, originatingElements);
	}

	Name getBinaryName(final TypeElement elm) {
		return processingEnv.getElementUtils().getBinaryName(elm);
	}

	TypeElement getTypeElement(final String name) {
		return processingEnv.getElementUtils().getTypeElement(name);
	}

	void printMessage(final Diagnostic.Kind kind, final CharSequence msg) {
		processingEnv.getMessager().printMessage(kind, msg);
	}

	void printMessage(final Diagnostic.Kind kind, final CharSequence msg, final Element e) {
		processingEnv.getMessager().printMessage(kind, msg, e);
	}
}
