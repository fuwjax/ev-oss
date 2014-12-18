package org.echovantage.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class AccessMapDecorator<K, T> extends AbstractMap<K, T> {
	private final Map<K, T> map;
	private final Function<? super K, ? extends T> tempFunction;

	public AccessMapDecorator(final Map<K, T> map, final Function<? super K, ? extends T> tempFunction) {
		this.map = map;
		this.tempFunction = tempFunction;
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
		return map.containsValue(value);
	}

	@Override
	public T get(final Object key) {
		return map.containsKey(key) ? map.get(key) : tempFunction.apply((K)key);
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(map.keySet());
	}

	@Override
	public Collection<T> values() {
		return Collections.unmodifiableCollection(map.values());
	}

	@Override
	public Set<Entry<K, T>> entrySet() {
		return Collections.unmodifiableSet(map.entrySet());
	}
}
