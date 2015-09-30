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
package org.fuwjax.oss.compile.file;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Supplier;

/**
 * A JavaFileObject for storing bytecode and source in memory.
 */
public class ReaderSourceFileObject extends BaseFileObject {
	private final Supplier<Reader> reader;

	public ReaderSourceFileObject(final String name, final Supplier<Reader> reader) {
		super(name, Kind.SOURCE, null);
		this.reader = reader;
	}

	@Override
	public Reader openReader(final boolean ignoreEncodingErrors) throws IOException {
		return reader.get();
	}
}
