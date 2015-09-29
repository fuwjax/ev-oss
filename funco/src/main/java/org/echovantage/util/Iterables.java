package org.echovantage.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class Iterables {
	public static <T> Iterable<T> over(final T[] array, final int offset, final int length) {
		return () -> new Iterator<T>() {
			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < length;
			}

			@Override
			public T next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return array[offset + index++];
			}
		};
	}

	public static <T> Iterable<T> over(final Stream<T> stream) {
		return stream::iterator;
	}

}
