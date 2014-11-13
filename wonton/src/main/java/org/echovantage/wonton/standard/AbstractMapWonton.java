package org.echovantage.wonton.standard;

import static org.echovantage.wonton.standard.StringWonton.escape;

import java.util.Map;

import org.echovantage.wonton.Wonton;

public abstract class AbstractMapWonton extends AbstractContainerWonton {
	@Override
	public final Type type() {
		return Type.STRUCT;
	}

	@Override
	public abstract Map<String, Wonton> asStruct();

	@Override
	protected final Wonton getShallow(final String shallowKey) {
		return asStruct().get(shallowKey);
	}

	@Override
	protected final void acceptShallow(final Visitor visitor) {
		for(final Map.Entry<String, Wonton> entry : asStruct().entrySet()) {
			visitor.visit(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder("{");
		String delim = "";
		for(final Map.Entry<String, ? extends Wonton> entry : asStruct().entrySet()) {
			builder.append(delim).append(escape(entry.getKey())).append(":").append(entry.getValue());
			delim = ",";
		}
		return builder.append("}").toString();
	}
}
