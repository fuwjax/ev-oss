package org.echovantage.metafactory;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Supplier;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public interface MetaInfContext {
	public interface MetaInfContract {
		boolean watch(final Element origin);

		void add(final String serviceName);

		void add(final TypeElement service);
	}

	TypeElement typeOf(Supplier<Class<?>> type);

	MetaInfContract getContract(TypeElement contract);

	Collection<? extends Element> getElementsAnnotatedWith(TypeElement annotation);

	JavaFileObject createSourceFile(final CharSequence name, final Element... originatingElements) throws IOException;
}
