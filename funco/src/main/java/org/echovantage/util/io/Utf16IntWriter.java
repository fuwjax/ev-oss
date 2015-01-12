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
