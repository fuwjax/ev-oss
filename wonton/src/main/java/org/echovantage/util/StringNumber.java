package org.echovantage.util;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class StringNumber extends Number {
	private final String number;

	public StringNumber(final String number) {
		this.number = number;
	}

	@Override
	public int intValue() {
		return parseInt(number);
	}

	@Override
	public long longValue() {
		return parseLong(number);
	}

	@Override
	public float floatValue() {
		return parseFloat(number);
	}

	@Override
	public double doubleValue() {
		return parseDouble(number);
	}

	@Override
	public String toString() {
		return number;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(doubleValue());
	}

	@Override
	public boolean equals(final Object obj) {
		try {
			final Number o = (Number)obj;
			return doubleToLongBits(o.doubleValue()) == doubleToLongBits(doubleValue());
		} catch(final Exception e) {
			return false;
		}
	}
}
