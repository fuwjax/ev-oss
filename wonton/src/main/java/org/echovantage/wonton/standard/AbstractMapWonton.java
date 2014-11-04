package org.echovantage.wonton.standard;

import java.util.Map;

import org.echovantage.wonton.Wonton;

public abstract class AbstractMapWonton extends AbstractContainerWonton {
	public AbstractMapWonton(final Type type) {
		super(type);
	}

	@Override
	public abstract Map<String, Wonton> asObject();

	@Override
	protected Wonton getShallow(final String shallowKey) {
		return asObject().get(shallowKey);
	}

	@Override
	protected void acceptShallow(final Visitor visitor) {
		for(final Map.Entry<String, Wonton> entry : asObject().entrySet()) {
			visitor.visit(entry.getKey(), entry.getValue());
		}
	}
}
