package org.echovantage.wonton.standard;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import org.echovantage.wonton.Wonton;

public class ListWonton extends AbstractListWonton {
	private final List<Wonton> values = new ArrayList<>();

	@Override
	public List<Wonton> asArray() {
		return unmodifiableList(values);
	}

	public void add(final Wonton wonton) {
		assert wonton != null;
		values.add(wonton);
	}
}
