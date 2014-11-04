package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

public class NumberWonton extends AbstractWonton {
	public static Wonton create(final Object value, final Type type) {
		return value instanceof Number ? new NumberWonton((Number) value, type) : null;
	}

	private final Number value;

	public NumberWonton(final Number value, final Wonton.Type type) {
		super(type);
		assert value != null;
		this.value = value;
	}

	@Override
	public Number asNumber() {
		return value;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(value.doubleValue());
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
