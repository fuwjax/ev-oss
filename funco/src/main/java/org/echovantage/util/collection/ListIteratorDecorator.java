/**
 * Copyright (C) 2014 EchoVantage (info@echovantage.com)
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

import java.util.ListIterator;
import java.util.function.Function;

public final class ListIteratorDecorator<O, T> implements ListIterator<T> {
	private final ListIterator<? extends O> iterator;
	private final Function<? super O, ? extends T> encoder;

	public ListIteratorDecorator(final ListIterator<? extends O> iterator, final Function<? super O, ? extends T> encoder) {
		this.iterator = iterator;
		this.encoder = encoder;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		return encoder.apply(iterator.next());
	}

	@Override
	public boolean hasPrevious() {
		return iterator.hasPrevious();
	}

	@Override
	public T previous() {
		return encoder.apply(iterator.previous());
	}

	@Override
	public int nextIndex() {
		return iterator.nextIndex();
	}

	@Override
	public int previousIndex() {
		return iterator.previousIndex();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(final T e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(final T e) {
		throw new UnsupportedOperationException();
	}
}
