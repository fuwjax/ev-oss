package org.echovantage.wonton.standard;

import java.util.List;

import org.echovantage.util.ListDecorator;
import org.echovantage.wonton.Wonton;

public class ListWrapper extends AbstractListWonton {
	private final List<Wonton> list;

	public ListWrapper(final List<?> original) {
		assert original != null;
		list = new ListDecorator<>(original, StandardFactory.FACTORY::wrap);
	}

	@Override
	public List<Wonton> asArray() {
		return list;
	}
}
