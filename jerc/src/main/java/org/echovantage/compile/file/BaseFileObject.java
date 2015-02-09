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
package org.echovantage.compile.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

/**
 * A JavaFileObject for storing bytecode and source in memory.
 */
public abstract class BaseFileObject implements JavaFileObject {
	private static final String URI_DELIMITER = ":///"; //$NON-NLS-1$
	private final String name;
	private final Kind kind;
	private final Charset charset;

	protected BaseFileObject(final String name, final Kind kind, final Charset charset) {
		this.name = name;
		this.kind = kind;
		this.charset = charset;
	}

	@Override
	public URI toUri() {
		return toUri(name, kind);
	}

	public static URI toUri(final String name, final Kind kind) {
		final String path = name.endsWith(kind.extension) ? name : name.replace('.', '/') + kind.extension;
		return URI.create(kind + URI_DELIMITER + path);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws IOException {
		final StringBuilder builder = new StringBuilder();
		final BufferedReader reader = new BufferedReader(openReader(ignoreEncodingErrors));
		String line = reader.readLine();
		while(line != null) {
			builder.append(line).append('\n');
			line = reader.readLine();
		}
		return builder;
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public boolean isNameCompatible(final String simpleName, final Kind kind) {
		final String baseName = simpleName + kind.extension;
		final String path = toUri().getPath();
		return kind.equals(getKind())
				&& (baseName.equals(path)
						|| path.endsWith("/" + baseName));
	}

	@Override
	public NestingKind getNestingKind() {
		return null;
	}

	@Override
	public Modifier getAccessLevel() {
		return null;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream openInputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader openReader(final boolean ignoreEncodingErrors) throws IOException {
		return new InputStreamReader(openInputStream(), getDecoder(ignoreEncodingErrors));
	}

	public CharsetDecoder getDecoder(final boolean ignoreEncodingErrors) {
		final CodingErrorAction action = ignoreEncodingErrors ? CodingErrorAction.REPLACE : CodingErrorAction.REPORT;
		return charset.newDecoder().onMalformedInput(action).onUnmappableCharacter(action);
	}

	@Override
	public Writer openWriter() throws IOException {
		return new OutputStreamWriter(openOutputStream(), charset);
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public boolean delete() {
		return false;
	}

	public static Kind kindOf(final String className) {
		for(final Kind kind : Kind.values()) {
			if(className.endsWith(kind.extension)) {
				return kind;
			}
		}
		return Kind.OTHER;
	}
}
