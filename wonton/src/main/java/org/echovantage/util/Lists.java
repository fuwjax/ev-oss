package org.echovantage.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class Lists {
	public static <T> List<T> toList(final Iterable<T> iter) {
		return toList(iter, new ArrayList<>());
	}

	public static <T> List<T> toList(final Iterable<T> iter, final List<T> list) {
		iter.forEach(list::add);
		return list;
	}

	public static List<?> reflectiveList(final Object array) {
		return new AbstractList<Object>() {
			@Override
			public Object get(final int index) {
				return Array.get(array, index);
			}

			@Override
			public int size() {
				return Array.getLength(array);
			}
		};
	}
}
