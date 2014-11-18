package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

public class BooleanWonton extends AbstractWonton {
	public static final Wonton TRUE = new BooleanWonton(true);
	public static final Wonton FALSE = new BooleanWonton(false);
	private final boolean value;

	public BooleanWonton(final boolean value) {
		this.value = value;
	}

	@Override
	public Type type() {
		return Type.BOOLEAN;
	}

	@Override
	public Boolean asBoolean() {
		return value;
	}

	@Override
	public String toString() {
		return value ? "true" : "false";
	}
}
