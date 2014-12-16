/*******************************************************************************
 * Copyright (c) 2011 Michael Doberenz. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: Michael Doberenz -
 * initial API and implementation
 ******************************************************************************/
package org.echovantage.util;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.util.Arrays.binarySearch;

import java.util.function.IntPredicate;

/**
 * A partition set for code points. Partition sets are an attempt at managing a
 * set of values by tracking ranges instead of individual elements. This set
 * tracks int ranges under the assumption that they represent code points.
 */
public class IntSet implements IntPredicate {
	private int[] values = new int[10];
	private int size;

	/**
	 * Clears the set.
	 */
	public void clear() {
		size = 0;
	}

	@Override
	public boolean test(final int codePoint) {
		return contains(codePoint);
	}

	/**
	 * Returns true if this set contains the code point.
	 * @param codePoint the code point
	 * @return true if the set contains the code point, false otherwise
	 */
	public boolean contains(final int codePoint) {
		int index = binarySearch(values, 0, size, codePoint);
		return index >= 0 || index % 2 == 0;
	}

	@Override
	public String toString() {
		return toString(16);
	}

	private String toString(final int radix) {
		final StringBuilder builder = new StringBuilder();
		for(int i = 0; i < size; i += 2) {
			builder.append(toString(i, radix)).append(',');
		}
		return builder.toString();
	}

	public IntSet addAll(final int... codePoints) {
		for(int codePoint : codePoints) {
			add(codePoint);
		}
		return this;
	}

	public IntSet addAll(final IntSet set) {
		for(int i = 0; i < set.size; i += 2) {
			add(set.values[i], set.values[i + 1]);
		}
		return this;
	}

	public IntSet add(final int codePoint) {
		return add(codePoint, codePoint);
	}

	public IntSet add(final int loCp, final int hiCp) {
		int lo = abs(binarySearch(values, 0, size, loCp));
		int hi = lo == size || hiCp <= values[lo] ? lo : abs(binarySearch(values, lo, size, hiCp));
		lo += lo % 2 == 1 ? -1 : lo > 0 && values[lo - 1] + 1 >= loCp ? -2 : 0;
		hi += hi % 2 == 1 ? 0 : hi < size && values[hi] - 1 <= hiCp ? 1 : -1;
		if(hi < lo) {
			int[] newValues = values;
			if(size >= values.length) {
				newValues = new int[values.length * 2];
				arraycopy(values, 0, newValues, 0, lo);
			}
			arraycopy(values, lo, newValues, lo + 2, size - lo);
			size += 2;
			values = newValues;
			values[lo] = loCp;
			values[lo + 1] = hiCp;
		} else {
			if(hi > lo + 1) {
				arraycopy(values, hi, values, lo + 1, size - hi);
				size -= hi - lo - 1;
			}
			values[lo] = min(loCp, values[lo]);
			values[lo + 1] = max(hiCp, values[lo + 1]);
		}
		return this;
	}

	private static int abs(final int result) {
		return result >= 0 ? result : -result - 1;
	}

	private String toString(final int index, final int radix) {
		if(values[index] == values[index + 1]) {
			return Integer.toString(values[index], radix);
		}
		return Integer.toString(values[index], radix) + "-" + Integer.toString(values[index + 1], radix);
	}
}
