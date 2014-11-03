package org.echovantage.wonton.standard;

import org.echovantage.wonton.StandardType;

public class BooleanWonton extends AbstractWonton {
	public static final BooleanWonton TRUE = new BooleanWonton(true);
	public static final BooleanWonton FALSE = new BooleanWonton(false);
	private final boolean value;

	private BooleanWonton(final boolean value) {
		// doubleton?
		this.value = value;
	}

	@Override
	public Boolean asBoolean() {
		return value;
	}

	@Override
	public Type type() {
		return StandardType.BOOLEAN;
	}
}
