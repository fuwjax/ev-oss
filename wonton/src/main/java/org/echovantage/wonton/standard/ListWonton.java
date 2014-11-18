package org.echovantage.wonton.standard;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.Wonton.Mutable;

public class ListWonton extends AbstractListWonton implements Mutable {
	private final List<Wonton> values = new ArrayList<>();

	@Override
	public List<Wonton> asArray() {
		return unmodifiableList(values);
	}

	@Override
	public ListWonton append(final Wonton wonton) {
		assert wonton != null;
		values.add(wonton);
		return this;
	}

	@Override
	protected ListWonton set(final String key, final Wonton value) {
		return put(Integer.parseInt(key), value);
	}

	public ListWonton put(final int index, final Wonton value) {
		while(index > values.size()) {
			values.add(NullWonton.NULL);
		}
		if(index == values.size()) {
			values.add(value);
		} else {
			values.set(index, value);
		}
		return this;
	}

	public ListWonton remove(final int index) {
		values.remove(index);
		return this;
	}

	@Override
	public Wonton build() {
		return super.build();
	}

	@Override
	public ListWonton append(final Path path, final Wonton value) {
		super.append(path, value);
		return this;
	}

	@Override
	public ListWonton set(final Path path, final Wonton value) {
		super.set(path, value);
		return this;
	}
}
