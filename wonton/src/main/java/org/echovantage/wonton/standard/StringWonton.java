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

public class StringWonton extends AbstractWonton implements Wonton.WString {
	public static String escape(final String value) {
		return '"' + value
				.replaceAll("\\\\", "\\\\")
				.replaceAll("/", "\\/")
				.replaceAll("\"", "\\\"")
				.replaceAll("\b", "\\b")
				.replaceAll("\f", "\\f")
				.replaceAll("\n", "\\n")
				.replaceAll("\r", "\\r")
				.replaceAll("\t", "\\t") + '"';
	}

	private final String value;

	public StringWonton(final String value) {
		assert value != null;
		this.value = value;
	}

	@Override
	public String asString() {
		return value;
	}

	@Override
	public String toString() {
		return escape(value);
	}
}
