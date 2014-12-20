package org.echovantage.util;

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
