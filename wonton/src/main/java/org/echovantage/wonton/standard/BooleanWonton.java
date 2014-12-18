package org.echovantage.wonton.standard;


import org.echovantage.wonton.Wonton;

public class BooleanWonton extends AbstractWonton implements Wonton.WBoolean{
	private final boolean value;

	public BooleanWonton(final boolean value) {
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
