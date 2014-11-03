package org.echovantage.wonton.standard;

import org.echovantage.wonton.StandardType;

public class StringWonton extends AbstractWonton {
	private final String value;

	public StringWonton(final String value) {
		this.value = value;
	}

	@Override
	public String asString() {
		return value;
	}

	@Override
	public Type type() {
		return StandardType.STRING;
	}
}
