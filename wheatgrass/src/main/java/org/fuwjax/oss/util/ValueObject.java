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

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.fuwjax.oss.util.function.Deferred;

public class ValueObject {
	private Deferred<Object[]> ids;
	
	protected final void identify(Object... ids){
		Object[] values = Arrays.copyOf(ids, ids.length);
		this.ids = new Deferred<>(() -> values);		
	}
	
	protected final void deferId(Supplier<?>...suppliers){
		List<Supplier<?>> values = Arrays.asList(suppliers);
		this.ids = new Deferred<>(() -> values.stream().map(Supplier::get).toArray(Object[]::new));
	}

	public boolean equals(final Object obj) {
		if(!(obj instanceof ValueObject)) {
			return false;
		}
		final ValueObject o = (ValueObject) obj;
		return Arrays.deepEquals(ids(), o.ids());
	}

	public int hashCode() {
		return Arrays.deepHashCode(ids());
	}

	public String toString() {
		return Arrays.deepToString(ids());
	}

	protected final Object[] ids() {
		return ids.get();
	}
}
