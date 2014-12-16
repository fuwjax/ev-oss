package org.echovantage.util.parser;

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
