package org.echovantage.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MapDecorator<K, O, T> extends AbstractMap<K, T> {
	private final Map<K, ? extends O> map;
	private final Function<? super O, ? extends T> encoder;

	public MapDecorator(final Map<K, ? extends O> map, final Function<? super O, ? extends T> encoder) {
		this.map = map;
		this.encoder = encoder;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return values().contains(value);
	}

	@Override
	public T get(final Object key) {
		return encoder.apply(map.get(key));
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(map.keySet());
	}

	@Override
	public Collection<T> values() {
		return new CollectionDecorator<>(map.values(), encoder);
	}

	@Override
	public Set<Map.Entry<K, T>> entrySet() {
		return new CollectionDecorator<>(map.entrySet(), entry -> new EntryDecorator<>(entry, encoder));
	}
}
