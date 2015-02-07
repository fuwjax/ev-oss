/**
 * Copyright (C) 2014 EchoVantage (info@echovantage.com)
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

import org.echovantage.metafactory.MetaInfContext.MetaInfContract;
import org.echovantage.util.Elements;

import javax.annotation.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.echovantage.util.Elements.functionalMethod;
import static org.echovantage.util.Elements.hasConstructor;
import static org.echovantage.util.Elements.invokeArgs;
import static org.echovantage.util.Elements.isAssignable;
import static org.echovantage.util.Elements.isInterface;
import static org.echovantage.util.Elements.packageOf;
import static org.echovantage.util.Elements.parameterDecl;
import static org.echovantage.util.Elements.throwsDecl;

public class MetaFactoryProcessor implements AnnotationProcessor {
	private final DateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private TypeElement contract;
	private ExecutableElement method;
	private MetaInfContract meta;
	private MetaInfContext context;

	@Override
	public synchronized boolean process(final TypeElement annotation, final MetaInfContext context) throws IOException {
		final MetaFactory mf = annotation.getAnnotation(MetaFactory.class);
		if(mf == null) {
			return false;
		}
		this.context = context;
		contract = context.typeOf(mf::value);
		method = functionalMethod(contract);
		meta = context.getContract(contract);
		for(final Element elm : context.getElementsAnnotatedWith(annotation)) {
			final TypeElement service = (TypeElement)elm;
			process(service);
		}
		return true;
	}

	private void process(final TypeElement service) throws IOException {
		if(isGenerated(service)) {
			return;
		}
		if(!isAssignable(Elements.typeOf(method.getReturnType()), service)) {
			throw new IllegalArgumentException(String.format("%s is not a %s, service does not satisfy factory method signature", service, method.getReturnType()));
		} else if(!hasConstructor(service, method)) {
			throw new IllegalArgumentException(String.format("%s does not have a constructor that matches the signature of %s", service, method));
		} else {
			add(service);
		}
	}

	private void add(final TypeElement service) throws IOException {
		if(meta.watch(service)) {
			final String name = service.getQualifiedName() + "CgFactory";
			final JavaFileObject f = context.createSourceFile(name, contract, service);
			writeFactory(f, service);
			meta.add(name);
		}
	}

	private void writeFactory(final JavaFileObject f, final TypeElement service) throws IOException {
		try(final PrintWriter pw = new PrintWriter(f.openWriter())) {
			pw.printf("package %s;", packageOf(service)).println();
			service.getAnnotationMirrors().forEach(pw::println);
			pw.printf("@javax.annotation.Generated(value=\"%s\", date=\"%s\")", genFlag(), ISO8601.format(new Date())).println();
			pw.printf("public final class %sCgFactory %s %s {", service.getSimpleName(), isInterface(contract) ? "implements" : "extends", contract).println();
			pw.printf("	public %s %s(%s)%s {", service.getSimpleName(), method.getSimpleName(), parameterDecl(method), throwsDecl(method)).println();
			pw.printf("		return new %s(%s);", service.getSimpleName(), invokeArgs(method)).println();
			pw.println("	}");
			pw.println("}");
		}
	}

	private static boolean isGenerated(final TypeElement service) {
		final Generated generated = service.getAnnotation(Generated.class);
		return generated != null && Arrays.asList(generated.value()).contains(genFlag());
	}

	private static String genFlag() {
		return MetaFactoryProcessor.class.getCanonicalName();
	}
}
