package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

public class BooleanWonton extends AbstractWonton {
	public static Wonton create(final Object value, final Type type) {
		return value instanceof Boolean ? new BooleanWonton((Boolean) value, type) : null;
	}

	private final boolean value;

	public BooleanWonton(final boolean value, final Wonton.Type type) {
		super(type);
		this.value = value;
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
