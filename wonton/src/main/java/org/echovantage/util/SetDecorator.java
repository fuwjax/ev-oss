package org.echovantage.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class SetDecorator<O, T> extends AbstractSet<T> {
	private final Set<? extends O> set;
	private final Function<? super O, ? extends T> encoder;

	public SetDecorator(final Set<? extends O> set, final Function<? super O, ? extends T> encoder) {
		this.set = set;
		this.encoder = encoder;
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return new IteratorDecorator<>(set.iterator(), encoder);
	}
}
