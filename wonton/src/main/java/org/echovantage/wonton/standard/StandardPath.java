package org.echovantage.wonton.standard;

import static org.echovantage.util.Strings.nullOrEmpty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.echovantage.wonton.Wonton.Path;

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

	private static final Path EMPTY = new StandardPath();
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
