package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

public class NumberWonton extends AbstractWonton {
	private final Number value;

	public NumberWonton(final Number value) {
		assert value != null;
		this.value = value;
	}

	@Override
	public Type type() {
		return Type.NUMBER;
	}

	@Override
	public Number asNumber() {
		return value;
	}

	@Override
	protected Object id(final Wonton wonton) {
		return wonton.asNumber().doubleValue();
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
