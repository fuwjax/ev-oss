/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
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
package org.fuwjax.oss.compile;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.tools.JavaFileObject;

import org.fuwjax.oss.compile.file.ByteArraySourceFileObject;
import org.fuwjax.oss.compile.file.ReaderSourceFileObject;
import org.fuwjax.oss.compile.file.StreamSourceFileObject;
import org.fuwjax.oss.compile.file.StringSourceFileObject;

public class RuntimeCompiler {
	private final Charset UTF_8 = Charset.forName("UTF-8");
	private final Set<JavaFileObject> files = new HashSet<>();

	public RuntimeCompiler include(final String name, final byte[] source) {
		return include(new ByteArraySourceFileObject(name, source, UTF_8));
	}

	public RuntimeCompiler include(final String name, final CharSequence source) {
		return include(new StringSourceFileObject(name, source));
	}

	public RuntimeCompiler includeStream(final String name, final Supplier<InputStream> source) {
		return include(new StreamSourceFileObject(name, source, UTF_8));
	}

	public RuntimeCompiler include(final String name, final Supplier<Reader> source) {
		return include(new ReaderSourceFileObject(name, source));
	}

	public RuntimeCompiler include(final JavaFileObject source) {
		files.add(source);
		return this;
	}

	public Map<String, Class<?>> compile() {
		final RuntimeClassLoader loader = new RuntimeClassLoader();
		if(!loader.compile(files)) {
			throw new RuntimeException("could not compile files");
		}
		final Map<String, Class<?>> classes = new HashMap<>();
		for(final JavaFileObject file : files) {
			try {
				classes.put(file.getName(), loader.loadClass(file.getName()));
			} catch(final ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return classes;
	}
}
