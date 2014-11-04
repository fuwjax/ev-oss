package org.echovantage.wonton.standard;

import static java.util.Collections.unmodifiableList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.echovantage.wonton.Wonton;

public class ListWonton extends AbstractListWonton implements Wonton.Mutable {
	public static Wonton create(final Object array, final Wonton.Type type, final Factory factory) {
		ListWonton wonton = null;
		if(array != null) {
			if(array instanceof Iterable) {
				wonton = new ListWonton(type);
				for(final Object e : (Iterable<?>) array) {
					wonton.add(factory.create(e));
				}
			} else if(array instanceof Object[]) {
				wonton = new ListWonton(type);
				for(final Object e : (Object[]) array) {
					wonton.add(factory.create(e));
				}
			} else if(array.getClass().isArray()) {
				wonton = new ListWonton(type);
				for(int i = 0; i < Array.getLength(array); i++) {
					wonton.add(factory.create(Array.get(array, i)));
				}
			}
		}
		return wonton;
	}

	private final List<Wonton> values = new ArrayList<>();

	public ListWonton(final Type type) {
		super(type);
	}

	@Override
	public List<Wonton> asArray() {
		return unmodifiableList(values);
	}

	public void add(final Wonton wonton) {
		assert wonton != null;
		values.add(wonton);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		String delim = "";
		for(final Wonton v : values) {
			builder.append(delim).append(v);
			delim = ",";
		}
		return builder.append("]").toString();
	}

	@Override
	protected void setShallow(final String shallowKey, final Wonton value) {
		int index = Integer.parseInt(shallowKey);
		while(index < values.size()) {
			values.add(null);
		}
		if(index == values.size()) {
			values.add(value);
		} else {
			values.set(index, value);
		}
	}
}
