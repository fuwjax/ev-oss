package org.echovantage.wonton.standard;

import static java.lang.Integer.parseInt;

import java.util.List;

import org.echovantage.wonton.Wonton;

public abstract class AbstractListWonton extends AbstractContainerWonton {
	public AbstractListWonton(final Type type) {
		super(type);
	}

	@Override
	public abstract List<Wonton> asArray();

	@Override
	protected void acceptShallow(final Visitor visitor) {
		int index = 0;
		for(final Wonton v : asArray()) {
			final String k = "[" + index++ + "]";
			visitor.visit(k, v);
		}
	}

	@Override
	protected Wonton getShallow(final String shallowKey) {
		return asArray().get(parseInt(shallowKey));
	}
}
