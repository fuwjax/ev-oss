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
package org.echovantage.metafactory;

import com.sun.tools.javac.util.ServiceLoader;
import org.echovantage.metafactory.MetaInfContext.MetaInfContract;
import org.echovantage.util.Elements;

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

/**
 * @author Fuwjax
 */
@SupportedAnnotationTypes("*")
public class MetaAnnotationProcessor extends AbstractProcessor {
	private final Map<String, MetaContract> metas = new HashMap<>();
	private final ServiceLoader<AnnotationProcessor> processors = ServiceLoader.load(AnnotationProcessor.class, getClass().getClassLoader());

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
		if(!processors.iterator().hasNext()){
			throw new RuntimeException("Could not find any " + AnnotationProcessor.class.getName() + " services");
		}
		final MetaInfContext context = new MetaContext(roundEnv);
		for(final TypeElement annotation : annotations) {
			try {
				for(final AnnotationProcessor processor : processors) {
					if(processor.process(annotation, context)) {
						break;
					}
				}
			} catch(final IOException e) {
				printMessage(Kind.ERROR, String.format("IO error while processing annotation: %s", e.getLocalizedMessage()), annotation);
			} catch(final RuntimeException e) {
				printMessage(Kind.ERROR, String.format("Unexpected exception: %s: %s", e.getClass(), e.getLocalizedMessage()), annotation);
				e.printStackTrace();
			}
		}
		if(roundEnv.processingOver()) {
			metas.values().forEach(this::writeMetaInfServices);
		}
		return false;
	}

	private void writeMetaInfServices(final MetaContract c) {
		if(c.services.isEmpty()) {
			return;
		}
		printMessage(Kind.NOTE, String.format("Writing %s: %s", c.metaInfServices(), c.services));
		try {
			loadExisting(StandardLocation.CLASS_OUTPUT, c);
			final FileObject metainfServices = createResource(StandardLocation.CLASS_OUTPUT, "", c.metaInfServices(), c.origins());
			try(Writer writer = metainfServices.openWriter();
			      PrintWriter pw = new PrintWriter(writer)) {
				c.services.forEach(pw::println);
			}
		} catch(final IOException e) {
			printMessage(Kind.ERROR, String.format("IO error while writing to meta-inf/services: %s", e.getLocalizedMessage()), c.contract);
		} catch(final RuntimeException e) {
			printMessage(Kind.ERROR, String.format("Unexpected exception: %s: %s", e.getClass(), e.getLocalizedMessage()), c.contract);
			e.printStackTrace();
		}
	}

	private void loadExisting(Location location, final MetaContract c) throws IOException {
		try {
			final FileObject f = getResource(location, "", c.metaInfServices());
			try(InputStream input = f.openInputStream();
			      Reader reader = new InputStreamReader(input, "UTF-8");
			      final BufferedReader r = new BufferedReader(reader)) {
				String line;
				while((line = r.readLine()) != null) {
					c.services.add(line);
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
		System.out.println("INFO " + msg);
		processingEnv.getMessager().printMessage(kind, msg);
	}

	void printMessage(final Diagnostic.Kind kind, final CharSequence msg, final Element e) {
		System.err.println("ERROR " + msg);
		processingEnv.getMessager().printMessage(kind, msg, e);
	}
}
