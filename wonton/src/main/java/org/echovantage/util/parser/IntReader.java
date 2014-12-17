package org.echovantage.util.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

public interface IntReader {
	static IntReader codepoints(final CharSequence chars) {
		final OfInt iter = chars.codePoints().iterator();
		return new IntReader() {
			private int count;

			@Override
			public int read() throws IOException {
				if(iter.hasNext()) {
					count++;
					return iter.nextInt();
				}
				return -1;
			}

			@Override
			public String toString() {
				return chars.subSequence(count, chars.length()).toString();
			}
		};
	}

	static IntReader codepoints(final IntStream codepoints) {
		final OfInt iter = codepoints.iterator();
		return () -> iter.hasNext() ? iter.nextInt() : -1;
	}

	static IntReader utf8ToCodepoint(final IntReader bytes) {
		return new Utf8IntReader(bytes);
	}

	static IntReader utf8ToCodepoint(final InputStream bytes) {
		return utf8ToCodepoint(bytes::read);
	}

	static IntReader utf8ToCodepoint(final ByteBuffer bytes) {
		return utf8ToCodepoint(() -> bytes.hasRemaining() ? bytes.get() : -1);
	}

	static IntReader charToCodepoint(final IntReader chars) {
		return new Utf16IntReader(chars);
	}

	static IntReader charToCodepoint(final Reader reader) {
		return charToCodepoint(reader::read);
	}

	/**
	 * Returns the next byte, char, or codepoint from the stream.
	 * @return the next codepoint or -1 if the stream has ended
	 * @throws IOException if there is an error reading from the underlying
	 *            stream
	 */
	int read() throws IOException;
}