/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.util.collection;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MappedMap<K, T> extends AbstractMap<K, T> {
	private final Function<? super K, ? extends T> values;
	private final Set<K> keys;

	public MappedMap(final Set<K> keys, final Function<? super K, ? extends T> values) {
		this.keys = keys;
		this.values = values;
	}

	@Override
	public int size() {
		return keys.size();
	}

	@Override
	public boolean isEmpty() {
		return keys.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return keys.contains(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return keys.stream().map(values).anyMatch(v -> Objects.equals(v, value));
	}

	@Override
	public T get(final Object key) {
		return keys.contains(key) ? values.apply((K)key) : null;
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(keys);
	}

	@Override
	public Collection<T> values() {
		return Collections.unmodifiableCollection(keys.stream().map(values).collect(Collectors.toList()));
	}

	@Override
	public Set<Entry<K, T>> entrySet() {
		return Collections.unmodifiableSet(keys.stream().map(k -> new SimpleEntry<K, T>(k, values.apply(k))).collect(Collectors.toSet()));
	}
}
