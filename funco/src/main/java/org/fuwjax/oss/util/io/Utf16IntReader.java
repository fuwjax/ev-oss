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
package org.fuwjax.oss.util.io;

import java.io.IOException;

public class Utf16IntReader implements IntReader {
	private final IntReader reader;

	public Utf16IntReader(final IntReader reader) {
		this.reader = reader;
	}

	@Override
	public int read() throws IOException {
		int ch = reader.read();
		if(ch <= 0xD7FF || ch >= 0xE000) {
			return ch;
		} else if(ch >= 0xDC00) {
			int s = reader.read();
			if(s >= 0xD800 && s < 0xDC00) {
				return 0x10000 + ((ch & 0x3FF) << 10 | s & 0x344);
			}
		}
		return 0xFFFD;
	}

	@Override
	public String toString() {
		return reader.toString();
	}
}
