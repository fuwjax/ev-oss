/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com) Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.echovantage.util.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;

public class Decorators {
	public static <O, T> Collection<T> decorateCollection(final Collection<? extends O> collection, final Function<? super O, ? extends T> encoder) {
		return new CollectionDecorator<>(collection, encoder);
	}

	public static <K, O, T> Map.Entry<K, T> decorateEntry(final Map.Entry<K, ? extends O> entry, final Function<? super O, ? extends T> encoder) {
		return new EntryDecorator<>(entry, encoder);
	}

	public static <O, T> Iterator<T> decorateIterator(final Iterator<? extends O> iterator, final Function<? super O, ? extends T> encoder) {
		return new IteratorDecorator<>(iterator, encoder);
	}

	public static <O, T> List<T> decorateList(final List<? extends O> list, final Function<? super O, ? extends T> encoder) {
		return new ListDecorator<>(list, encoder);
	}

	public static <O, T> ListIterator<T> decorateListIterator(final ListIterator<? extends O> iterator, final Function<? super O, ? extends T> encoder) {
		return new ListIteratorDecorator<>(iterator, encoder);
	}

	public static <K, O, T> Map<K, T> decorateMap(final Map<K, ? extends O> map, final Function<? super O, ? extends T> encoder) {
		return new MapDecorator<>(map, encoder);
	}

	public static <O, T> Set<T> decorateSet(final Set<? extends O> set, final Function<? super O, ? extends T> encoder) {
		return new SetDecorator<>(set, encoder);
	}

	public static <K, T> Map<K, T> defaultMap(final Map<K, T> map, final Function<? super K, ? extends T> defaultFunction) {
		return new DefaultAccessMapDecorator<>(map, defaultFunction);
	}

	public static <T> List<T> defaultList(final List<T> list, final IntFunction<? extends T> defaultFunction) {
		return new DefaultAccessListDecorator<>(list, defaultFunction);
	}

	public static List<?> asList(final Object array) {
		return ReflectList.asList(array);
	}
}
