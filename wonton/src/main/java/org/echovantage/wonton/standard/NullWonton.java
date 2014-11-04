package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

public class NullWonton extends AbstractWonton {
	public static Wonton create(final Object value, final Wonton.Type type) {
		return value == null ? new NullWonton(type) : null;
	}

	public NullWonton(final Wonton.Type type) {
		super(type);
	}

	@Override
	public String toString() {
		return "null";
	}
}
