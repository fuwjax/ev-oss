package org.echovantage.util.collection;

import java.util.Iterator;
import java.util.function.Function;

public final class IteratorDecorator<O, T> implements Iterator<T> {
	private final Iterator<? extends O> iter;
	private final Function<? super O, ? extends T> encoder;

	public IteratorDecorator(final Iterator<? extends O> iterator, final Function<? super O, ? extends T> encoder) {
		this.iter = iterator;
		this.encoder = encoder;
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public T next() {
		return encoder.apply(iter.next());
	}
}
