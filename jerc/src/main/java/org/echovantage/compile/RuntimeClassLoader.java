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
package org.echovantage.compile;

import org.echovantage.compile.file.ClassFileObject;
import org.echovantage.compile.file.StringSourceFileObject;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static javax.tools.ToolProvider.getSystemJavaCompiler;
import static org.echovantage.compile.file.BaseFileObject.kindOf;
import static org.echovantage.compile.file.BaseFileObject.toUri;

/**
 * A ClassLoader which compiles source code at runtime in memory.
 */
public class RuntimeClassLoader extends ClassLoader {
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	private static final JavaCompiler compiler = getSystemJavaCompiler();
	private final ConcurrentMap<URI, ClassFileObject> files = new ConcurrentHashMap<>();
	private final ForwardingJavaFileManager<StandardJavaFileManager> manager = new ForwardingJavaFileManager<StandardJavaFileManager>(
			compiler.getStandardFileManager(null, null, null)) {
		@Override
		public FileObject getFileForOutput(final Location location, final String packageName, final String relativeName, final FileObject sibling) throws IOException {
			final String className = className(packageName, relativeName);
			return getJavaFileForOutput(location, className, kindOf(className), sibling);
		}

		private String className(final String packageName, final String relativeName) {
			return "".equals(packageName) ? relativeName : packageName.replaceAll("\\.", "/") + "/" + relativeName;
		}

		@Override
		public FileObject getFileForInput(final Location location, final String packageName, final String relativeName) throws IOException {
			final String className = className(packageName, relativeName);
			final Kind kind = kindOf(className);
			FileObject file = files.get(toUri(className, kind));
			if(file == null) {
				file = super.getFileForInput(location, packageName, relativeName);
				if(file == null) {
					file = new ClassFileObject(className, kind, UTF_8);
				}
			}
			return file;
		}

		@Override
		public JavaFileObject getJavaFileForInput(final Location location, final String className, final Kind kind) throws IOException {
			JavaFileObject file = files.get(toUri(className, kind));
			if(file == null) {
				file = super.getJavaFileForInput(location, className, kind);
				if(file == null) {
					file = new ClassFileObject(className, kind, UTF_8);
				}
			}
			return file;
		}

		@Override
		public boolean isSameFile(final FileObject a, final FileObject b) {
			return a.toUri().equals(b.toUri());
		}

		@Override
		public JavaFileObject getJavaFileForOutput(final Location location, final String className, final Kind kind,
				final FileObject sibling) throws IOException {
			final ClassFileObject file = new ClassFileObject(className, kind, UTF_8);
			final ClassFileObject old = files.putIfAbsent(toUri(className, kind), file);
			return old == null ? file : old;
		}
	};
	private final Writer log;

	public RuntimeClassLoader() {
		log = new StringWriter();
	}

	public RuntimeClassLoader(final OutputStream logStream) {
		log = new OutputStreamWriter(logStream);
	}

	public boolean compile(final Path root, final String... options) throws IOException {
		final Set<StringSourceFileObject> compUnit = new HashSet<>();
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				if(file.toString().endsWith(".java")) {
					final String name = root.relativize(file).toString();
					compUnit.add(new StringSourceFileObject(name, new String(Files.readAllBytes(file))));
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return compile(compUnit, options);
	}

	/**
	 * Compiles the {@code source} java code into the class {@code name}.
	 * @param sources the java source code
	 * @param options the compiler options
	 * @return true if the source code compiled, false otherwise
	 */
	public boolean compile(final Map<String, String> sources, final String... options) {
		final Set<StringSourceFileObject> compUnit = sources.entrySet().stream().map(entry -> new StringSourceFileObject(entry.getKey(), entry.getValue())).collect(Collectors.toSet());
		return compile(compUnit, options);
	}

	public boolean compile(final Set<? extends JavaFileObject> compUnit, final String... options) {
		return compiler.getTask(log, manager, null, asList(options), null, compUnit).call();
	}

	@Override
	protected URL findResource(final String name) {
		final ClassFileObject file = files.get(toUri(name, Kind.OTHER));
		if(file == null) {
			return null;
		}
		try {
			return new URL("memory", "localhost", -1, name, new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(final URL u) throws IOException {
					return new URLConnection(u) {
						@Override
						public void connect() throws IOException {
							// do nothing
						}

						@Override
						public InputStream getInputStream() throws IOException {
							return file.openInputStream();
						}
					};
				}
			});
		} catch(final MalformedURLException e) {
			throw new RuntimeException("RuntimeClassLoader bug - report immediately", e);
		}
	}

	@Override
	protected Enumeration<URL> findResources(final String name) throws IOException {
		return enumeration(singleton(findResource(name)));
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		final ClassFileObject file = files.get(toUri(name, Kind.CLASS));
		if(file == null) {
			throw new ClassNotFoundException(name);
		}
		final byte[] b = file.toBytes();
		return defineClass(name, b, 0, b.length);
	}
}
