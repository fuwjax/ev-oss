package org.echovantage.wonton.standard;

import static java.util.Collections.unmodifiableNavigableMap;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.Wonton.MutableStruct;

public class MapWonton extends AbstractMapWonton implements MutableStruct {
	private final NavigableMap<String, Wonton> entries = new TreeMap<>();

	@Override
	public NavigableMap<String, Wonton> asStruct() {
		return unmodifiableNavigableMap(entries);
	}

	@Override
	protected void setShallow(final String shallowKey, final Wonton value) {
		entries.put(shallowKey, value);
	}

	@Override
	public Wonton build() {
		return super.build();
	}

	@Override
	public void set(final String key, final Wonton value) {
		super.set(key, value);
	}
}
