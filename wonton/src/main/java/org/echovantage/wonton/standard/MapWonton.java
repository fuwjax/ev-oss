package org.echovantage.wonton.standard;

import static java.util.Collections.unmodifiableNavigableMap;
import static org.echovantage.wonton.standard.StringWonton.escape;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.echovantage.wonton.Wonton;

public class MapWonton extends AbstractMapWonton implements Wonton.Mutable {
	public static Wonton create(final Object value, final Wonton.Type type, final Factory factory) {
		if(value instanceof Map) {
			final MapWonton wonton = new MapWonton(type);
			((Map<?, ?>) value).entrySet().forEach(entry -> wonton.put(String.valueOf(entry.getKey()), factory.create(entry.getValue())));
			return wonton;
		}
		return null;
	}

	private final NavigableMap<String, Wonton> entries = new TreeMap<>();

	public MapWonton(final Type type) {
		super(type);
	}

	@Override
	public NavigableMap<String, Wonton> asObject() {
		return unmodifiableNavigableMap(entries);
	}

	public void put(final String key, final Wonton value) {
		assert key != null;
		assert value != null;
		entries.put(key, value);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("{");
		String delim = "";
		for(final Map.Entry<String, ? extends Wonton> entry : entries.entrySet()) {
			builder.append(delim).append(escape(entry.getKey())).append(":").append(entry.getValue());
			delim = ",";
		}
		return builder.append("}").toString();
	}

	@Override
	protected void setShallow(final String shallowKey, final Wonton value) {
		put(shallowKey, value);
	}
}
