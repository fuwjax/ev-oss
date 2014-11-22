package org.echovantage.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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

	@Override
	public boolean equals(final Object obj) {
		if(obj == null || !(obj instanceof Map.Entry)) {
			return false;
		}
		final Map.Entry o = (Map.Entry)obj;
		return Objects.equals(getKey(), o.getKey()) && Objects.equals(getValue(), o.getValue());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
	}
}
