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
package org.fuwjax.oss.util.function;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface UnsafePredicate<T> extends Predicate<T> {
	boolean testUnsafe(T t) throws Exception;

	@Override
	default boolean test(final T t) {
		try {
			return testUnsafe(t);
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException(e, "consumer did not accept safely");
		}
	}

	default UnsafePredicate<T> withToString(BiFunction<T, Boolean, String> toString){
		return new UnsafePredicate<T>(){
			public T lastT;
			public boolean lastResult;

			@Override
			public boolean testUnsafe(T t) throws Exception {
				lastResult = UnsafePredicate.this.testUnsafe(t);
				lastT = t;
				return lastResult;
			}

			@Override
			public String toString() {
				return toString.apply(lastT, lastResult);
			}
		};
	}
}
