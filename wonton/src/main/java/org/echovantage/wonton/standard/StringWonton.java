package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

public class StringWonton extends AbstractWonton {
	public static Wonton create(final Object value, final Type type) {
		return value instanceof CharSequence ? new StringWonton(value.toString(), type) : null;
	}

	public static String escape(final String value) {
		return '"' + value
				.replaceAll("\\\\", "\\\\")
				.replaceAll("/", "\\/")
				.replaceAll("\"", "\\\"")
				.replaceAll("\b", "\\b")
				.replaceAll("\f", "\\f")
				.replaceAll("\n", "\\n")
				.replaceAll("\r", "\\r")
				.replaceAll("\t", "\\t") + '"';
	}

	private final String value;

	public StringWonton(final String value, final Wonton.Type type) {
		super(type);
		this.value = value;
	}

	@Override
	public String asString() {
		return value;
	}

	@Override
	public String toString() {
		return escape(value);
	}
}
