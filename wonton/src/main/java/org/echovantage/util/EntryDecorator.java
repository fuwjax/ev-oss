package org.echovantage.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class EntryDecorator<K, O, T> implements Map.Entry<K, T> {
	private final Entry<K, ? extends O> entry;
	private final Function<? super O, ? extends T> encoder;

	public EntryDecorator(final Map.Entry<K, ? extends O> entry, final Function<? super O, ? extends T> encoder) {
		this.entry = entry;
		this.encoder = encoder;
	}

	@Override
	public K getKey() {
		return entry.getKey();
	}

	@Override
	public T getValue() {
		return encoder.apply(entry.getValue());
	}

	@Override
	public T setValue(final T value) {
		throw new UnsupportedOperationException();
	}
}
