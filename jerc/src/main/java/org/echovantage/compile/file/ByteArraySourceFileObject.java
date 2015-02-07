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
package org.echovantage.compile.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A JavaFileObject for storing bytecode and source in memory.
 */
public class ByteArraySourceFileObject extends BaseFileObject {
	private final byte[] bytes;

	public ByteArraySourceFileObject(final String name, final byte[] bytes, final Charset charset) {
		super(name, Kind.SOURCE, charset);
		this.bytes = bytes;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return new ByteArrayInputStream(bytes);
	}
}
