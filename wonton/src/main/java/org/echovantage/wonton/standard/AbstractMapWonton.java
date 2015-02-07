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
package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

import java.util.Map;

import static org.echovantage.wonton.standard.StringWonton.escape;

public abstract class AbstractMapWonton extends AbstractContainerWonton implements Wonton.WStruct {
	public static Wonton wrap(Map<String, ? extends Wonton> map){
		return new AbstractMapWonton() {
			@Override
			public Map<String, ? extends Wonton> asStruct() {
				return map;
			}
		};
	}

	@Override
	protected final void acceptShallow(final ShallowVisitor visitor) {
		for(final Map.Entry<String, ? extends Wonton> entry : asStruct().entrySet()) {
			visitor.visit(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder("{");
		String delim = "\n\t";
		for(final Map.Entry<String, ? extends Wonton> entry : asStruct().entrySet()) {
			builder.append(delim).append(escape(entry.getKey())).append(":").append(entry.getValue().toString().replaceAll("\n","\n\t"));
			delim = ",\n\t";
		}
		return builder.append("\n}").toString();
	}
}
