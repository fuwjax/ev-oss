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
package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

import java.util.Objects;

public abstract class AbstractWonton implements Wonton {
	@Override
	public abstract String toString();

	@Override
	public final boolean equals(final Object obj) {
		if(obj instanceof Wonton) {
			final Wonton o = (Wonton)obj;
			return type().equals(o.type()) && Objects.equals(id(this), id(o));
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(id(this));
	}

	protected Object id(final Wonton value) {
		return value.value();
	}
}
