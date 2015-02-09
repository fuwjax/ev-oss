/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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
package org.echovantage.util.collection;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

public final class ListDecorator<O, T> extends AbstractList<T> {
	private final List<? extends O> list;
	private final Function<? super O, ? extends T> encoder;

	public ListDecorator(final List<? extends O> list, final Function<? super O, ? extends T> encoder) {
		this.list = list;
		this.encoder = encoder;
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return new IteratorDecorator<>(list.iterator(), encoder);
	}

	@Override
	public T get(final int index) {
		return encoder.apply(list.get(index));
	}

	@Override
	public ListIterator<T> listIterator() {
		return new ListIteratorDecorator<>(list.listIterator(), encoder);
	}

	@Override
	public ListIterator<T> listIterator(final int index) {
		return new ListIteratorDecorator<>(list.listIterator(index), encoder);
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex) {
		return new ListDecorator<>(list.subList(fromIndex, toIndex), encoder);
	}
}
