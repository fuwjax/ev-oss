package org.echovantage.wonton.standard;


public class BooleanWonton extends AbstractWonton {
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
