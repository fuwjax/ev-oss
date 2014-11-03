package org.echovantage.wonton.standard;

import static java.util.Collections.unmodifiableNavigableMap;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.echovantage.wonton.Wonton;

public class MapWonton extends AbstractMapWonton {
	private final NavigableMap<String, Wonton> entries = new TreeMap<>();

	@Override
	public NavigableMap<String, Wonton> asObject() {
		return unmodifiableNavigableMap(entries);
	}

	public void put(final String key, final Wonton value) {
		assert key != null;
		assert value != null;
		entries.put(key, value);
	}
}
