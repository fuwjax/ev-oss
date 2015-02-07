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

import org.echovantage.wonton.Wonton.Path;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.echovantage.util.Strings.nullOrEmpty;

public class StandardPath implements Path {
	public static Path pathOf(final String... keys) {
		assert keys != null && keys.length > 0;
		return pathOf(0, keys);
	}

	private static Path pathOf(final int index, final String[] keys) {
		assert index >= keys.length || !nullOrEmpty(keys[index]);
		return index >= keys.length ? EMPTY : new StandardPath(keys[index], pathOf(index + 1, keys));
	}

	private static final Pattern KEY = Pattern.compile("^\\[?([^\\[.\\]]+)\\]?\\.?((?![\\].]).*)$");

	public static Path path(final String path) {
		assert !nullOrEmpty(path);
		final Matcher matcher = KEY.matcher(path);
		if(!matcher.matches()) {
			throw new IllegalArgumentException(path);
		}
		if(nullOrEmpty(matcher.group(2))) {
			return new StandardPath(matcher.group(1));
		}
		return new StandardPath(matcher.group(1), path(matcher.group(2)));
	}

	public static boolean isPath(String maybe){
		Matcher matcher = KEY.matcher(maybe);
		return matcher.matches() && !nullOrEmpty(matcher.group(2));
	}

	public static final Path EMPTY = new StandardPath();
	private final String key;
	private final Path tail;

	private StandardPath() {
		key = "";
		tail = this;
	}

	public StandardPath(final String key) {
		this(key, EMPTY);
	}

	public StandardPath(final String key, final Path tail) {
		assert !nullOrEmpty(key);
		assert tail != null;
		this.key = key;
		this.tail = tail;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public Path tail() {
		return tail;
	}

	@Override
	public Path append(final Path sub) {
		assert sub != null;
		return isEmpty() ? sub : new StandardPath(key, tail.append(sub));
	}

	@Override
	public boolean isEmpty() {
		return tail == this;
	}

	@Override
	public String toString() {
		if(key.matches("\\d+")) {
			return '[' + key + ']' + tail.toString();
		}
		if(tail.isEmpty()) {
			return key;
		}
		final String t = tail().toString();
		return t.startsWith("[") ? key + t : key + '.' + t;
	}
}
