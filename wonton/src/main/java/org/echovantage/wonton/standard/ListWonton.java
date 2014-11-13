package org.echovantage.wonton.standard;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.Wonton.MutableArray;

public class ListWonton extends AbstractListWonton implements MutableArray {
	private final List<Wonton> values = new ArrayList<>();

	@Override
	public List<Wonton> asArray() {
		return unmodifiableList(values);
	}

	@Override
	public void append(final Wonton wonton) {
		assert wonton != null;
		values.add(wonton);
	}

	@Override
	protected void setShallow(final String shallowKey, final Wonton value) {
		final int index = Integer.parseInt(shallowKey);
		while(index < values.size()) {
			values.add(null);
		}
		if(index == values.size()) {
			values.add(value);
		} else {
			values.set(index, value);
		}
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
