package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

public class StringWonton extends AbstractWonton implements Wonton.WString {
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
	public String asString() {
		return value;
	}

	@Override
	public String toString() {
		return escape(value);
	}
}
