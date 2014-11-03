package org.echovantage.wonton.standard;

import org.echovantage.wonton.StandardType;

public class NumberWonton extends AbstractWonton {
	private final Number value;

	public NumberWonton(final Number value) {
		assert value != null;
		this.value = value;
	}

	@Override
	public Number asNumber() {
		return value;
	}

	@Override
	public Type type() {
		return StandardType.NUMBER;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(value.doubleValue());
	}
}
