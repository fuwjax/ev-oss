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

public class Utf8IntReader implements IntReader {
	private final IntReader input;

	public Utf8IntReader(final IntReader utf8Bytes) {
		this.input = utf8Bytes;
	}

	@Override
	public int read() throws IOException {
		int c = input.read();
		if(c < 0x80) {
			return c;
		}
		// if c is a continuation byte
		// then return the replacement character
		if(c < 0xC0) {
			return 0xFFFD;
		}
		int mask;
		for(mask = 0x40; (c & mask) > 0; mask <<= 5) {
			int continuation = input.read();
			// if continuation is not a continuation byte
			// then return the replacement character
			// note that in this case a potential extra character has been
			// consumed however as it is malformed utf8, what should happen is
			// not well defined.
			if((continuation & 0xC0) != 0x80) {
				return 0xFFFD;
			}
			c = c << 6 | continuation & 0x3F;
		}
		c = c & mask - 1;
		// if c didn't need multi-byte encoding
		// or if c is bigger than the max codepoint
		// or if c is a high or low UTF-16 surrogate
		// or if c is an overlong encoding
		// then return the replacement character
		if(c < 0x80 || c > 0x10FFFF || c > 0xD7FF && c < 0xE000
		      || c < mask >> 5) {
			c = 0xFFFD;
		}
		return c;
	}

	@Override
	public String toString() {
		return input.toString();
	}
}
