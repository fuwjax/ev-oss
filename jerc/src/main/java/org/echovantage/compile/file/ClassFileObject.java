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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A JavaFileObject for storing bytecode and source in memory.
 */
public class ClassFileObject extends BaseFileObject {
	private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

	/**
	 * Creates a new instance.
	 * @param name the class name
	 * @param kind the class kind
	 * @param charset the charset
	 */
	public ClassFileObject(final String name, final Kind kind, final Charset charset) {
		super(name, kind, charset);
	}

	public byte[] toBytes() {
		return bytes.toByteArray();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return bytes;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		if(bytes.size() == 0) {
			throw new FileNotFoundException();
		}
		return new ByteArrayInputStream(toBytes());
	}
}
