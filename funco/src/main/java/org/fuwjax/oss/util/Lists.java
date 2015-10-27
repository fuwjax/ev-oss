/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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

	public static <T> Iterable<T> reverse(final List<T> list) {
		return () -> new Iterator<T>() {
			private final ListIterator<T> iter = list.listIterator(list.size());
			@Override
			public boolean hasNext() {
				return iter.hasPrevious();
			}

			@Override
			public T next() {
				return iter.previous();
			}

			@Override
			public void remove() {
				iter.remove();
			}
		};
	}
}
