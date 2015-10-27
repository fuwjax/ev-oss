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
package org.fuwjax.oss.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Iterables {
	public static <T> Iterable<T> over(final T[] array, final int offset, final int length) {
		return () -> new Iterator<T>() {
			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < length;
			}

			@Override
			public T next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return array[offset + index++];
			}
		};
	}

	public static <T> Iterable<T> over(final Stream<T> stream) {
		return stream::iterator;
	}

	public static <T> Iterable<T> over(final Iterator<T> iter) {
		return () -> iter;
	}
	
	public interface IndexConsumer<T>{
		void accept(T value, int index);
		
		static <T> IndexConsumer<T> consumer(BiConsumer<T, Integer> consumer){
			return (IndexConsumer<T>)consumer::accept;
		}
	}
	
	public static <T> void forEach(Iterable<T> iterable, IndexConsumer<T> consumer) {
		int index = 0;
		for(Iterator<T> iter = iterable.iterator(); iter.hasNext();){
			consumer.accept(iter.next(), index++);
		}
	}

	public static <T> void breakingForEach(Iterable<T> iterable, Predicate<T> consumer) {
		for(Iterator<T> iter = iterable.iterator(); iter.hasNext();){
			if(!consumer.test(iter.next())){
				break;
			}
		}
	}
}
