package org.echovantage.wonton.standard;

import static java.util.Collections.unmodifiableNavigableMap;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.Wonton.Mutable;

public class MapWonton extends AbstractMapWonton implements Mutable {
	private final NavigableMap<String, Wonton> entries = new TreeMap<>();

	@Override
	public NavigableMap<String, Wonton> asStruct() {
		return unmodifiableNavigableMap(entries);
	}

	@Override
	protected MapWonton append(final Wonton value) {
		final String key = Integer.toString(entries.size() - 1);
		if(entries.containsKey(key)) {
			throw new InvalidTypeException();
		}
		entries.put(key, value);
		return this;
	}

	@Override
	protected MapWonton set(final String shallowKey, final Wonton value) {
		entries.put(shallowKey, value);
		return this;
	}

	@Override
	public Wonton build() {
		if(entries.isEmpty()) {
			return this;
		}
		if(entries.containsKey("0") && entries.containsKey(Integer.toString(entries.size() - 1))) {
			final ListWonton list = new ListWonton();
			for(int i = 0; i < entries.size(); i++) {
				final Wonton w = entries.get(Integer.toString(i));
				list.append(w instanceof Mutable ? ((Mutable)w).build() : w);
			}
			return list;
		}
		for(final Map.Entry<String, Wonton> entry : entries.entrySet()) {
			if(entry.getValue() instanceof Mutable) {
				entry.setValue(((Mutable)entry.getValue()).build());
			}
		}
		return this;
	}

	@Override
	public MapWonton set(final Path path, final Wonton value) {
		super.set(path, value);
		return this;
	}

	@Override
	public MapWonton append(final Path path, final Wonton value) {
		super.append(path, value);
		return this;
	}
}
