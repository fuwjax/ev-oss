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

import java.util.*;
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
		return new SetDecorator<>(map.entrySet(), entry -> new EntryDecorator<>(entry, encoder));
	}
}
