package org.echovantage.wonton.standard;

public class StringWonton extends AbstractWonton {
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

	public StringWonton(final String value) {
		assert value != null;
		this.value = value;
	}

	@Override
	public Type type() {
		return Type.STRING;
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
