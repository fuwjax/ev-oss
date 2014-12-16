package org.echovantage.util.io;

public class CodepointBuffer {
	private final StringBuilder builder = new StringBuilder();

	public CodepointBuffer append(final int cp) {
		if(cp <= 0xFFFF) {
			builder.append((char) cp);
		} else {
			builder.append((char) (cp - 0x10000 >>> 10 | 0xD800)).append((char) (cp & 0x3FF | 0xDC00));
		}
		return this;
	}

	@Override
	public String toString() {
		return builder.toString();
	}

	public CodepointBuffer append(final String string) {
		builder.append(string);
		return this;
	}
}
