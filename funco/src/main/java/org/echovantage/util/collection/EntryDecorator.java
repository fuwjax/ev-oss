/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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
package org.echovantage.util.collection;

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
