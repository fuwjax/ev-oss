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
