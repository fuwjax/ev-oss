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
package org.echovantage.util.io;

import java.io.IOException;

import static org.echovantage.util.function.Functions.intConsumer;

public class Utf16IntWriter implements IntWriter{
	private final IntWriter writer;

	public Utf16IntWriter(final IntWriter writer) {
		this.writer = writer;
	}

	public void write(final int cp) throws IOException {
		if(cp <= 0xFFFF) {
			writer.write(cp);
		} else {
			writer.write(cp - 0x10000 >>> 10 | 0xD800);
			writer.write(cp & 0x3FF | 0xDC00);
		}
	}

	@Override
	public void write(CharSequence value) throws IOException {
		value.chars().forEach(intConsumer(this::write));
	}

	@Override
	public String toString() {
		return writer.toString();
	}
}
