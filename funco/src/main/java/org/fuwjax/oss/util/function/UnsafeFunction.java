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

import java.util.function.Function;

public interface UnsafeFunction<T, R> extends Function<T, R> {
	R applyUnsafe(T t) throws Exception;

	@Override
	default R apply(final T t) {
		try {
			return applyUnsafe(t);
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw new UnsafeException(e, "function did not apply safely");
		}
	}

	default UnsafeSupplier<R> defer(T value) {
		return () -> applyUnsafe(value);
	}
}
