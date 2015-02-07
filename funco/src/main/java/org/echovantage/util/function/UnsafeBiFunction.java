/**
 * Copyright (C) 2014 EchoVantage (info@echovantage.com)
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
package org.echovantage.util.function;

import java.util.function.BiFunction;

public interface UnsafeBiFunction<T, U, R> extends BiFunction<T, U, R> {
	R applyUnsafe(T t, U u) throws Exception;

	@Override
	default R apply(final T t, final U u) {
		try {
			return applyUnsafe(t, u);
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException(e, "biFunction did not apply safely");
		}
	}
}
