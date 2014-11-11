package org.echovantage.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class CollectionDecorator<O, T> extends AbstractCollection<T> implements Set<T> {
	private final Collection<? extends O> collection;
	private final Function<? super O, ? extends T> encoder;

	public CollectionDecorator(final Collection<? extends O> collection, final Function<? super O, ? extends T> encoder) {
		this.collection = collection;
		this.encoder = encoder;
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return new IteratorDecorator<>(collection.iterator(), encoder);
	}
}
