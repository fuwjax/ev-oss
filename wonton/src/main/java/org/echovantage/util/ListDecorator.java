package org.echovantage.util;

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
		return new IteratorDecorator(list.iterator(), encoder);
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
