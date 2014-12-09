package org.echovantage.compile;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.tools.JavaFileObject;

import org.echovantage.compile.file.ByteArraySourceFileObject;
import org.echovantage.compile.file.ReaderSourceFileObject;
import org.echovantage.compile.file.StreamSourceFileObject;
import org.echovantage.compile.file.StringSourceFileObject;

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
