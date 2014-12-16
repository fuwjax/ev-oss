package org.echovantage.util.parser;

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
