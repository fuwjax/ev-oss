package org.echovantage.util.collection;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.IntFunction;

public final class DefaultAccessListDecorator<T> extends AbstractList<T> {
	private final List<T> list;
	private final IntFunction<? extends T> function;

	public DefaultAccessListDecorator(final List<T> list, final IntFunction<? extends T> function) {
		this.list = list;
		this.function = function;
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
		return list.iterator();
	}

	@Override
	public T get(final int index) {
		return index < list.size() ? list.get(index) : function.apply(index);
	}

	@Override
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(final int index) {
		return list.listIterator(Math.min(index, list.size()));
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex) {
		return new DefaultAccessListDecorator<>(list.subList(Math.min(fromIndex, list.size()), Math.min(toIndex, list.size())), function);
	}
}
