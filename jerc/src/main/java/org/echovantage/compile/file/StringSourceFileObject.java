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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * A JavaFileObject for storing bytecode and source in memory.
 */
public class StringSourceFileObject extends BaseFileObject {
	private final CharSequence source;

	/**
	 * Creates a new instance.
	 * @param name the name of the class
	 * @param source the java source code
	 */
	public StringSourceFileObject(final String name, final CharSequence source) {
		super(name, Kind.SOURCE, null);
		this.source = source;
	}

	@Override
	public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws IOException {
		return source;
	}

	@Override
	public Reader openReader(final boolean ignoreEncodingErrors) throws IOException {
		return new StringReader(source.toString());
	}
}
