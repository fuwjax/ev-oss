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
