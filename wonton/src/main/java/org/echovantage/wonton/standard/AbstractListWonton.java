package org.echovantage.wonton.standard;

import static java.lang.Integer.parseInt;

import java.util.List;

import org.echovantage.wonton.Wonton;

public abstract class AbstractListWonton extends AbstractContainerWonton {
	@Override
	public abstract List<? extends Wonton> asArray();

	@Override
	public final Type type() {
		return Type.ARRAY;
	}

	@Override
	protected final void acceptShallow(final ShallowVisitor visitor) {
		int index = 0;
		for(final Wonton v : asArray()) {
			visitor.visit(Integer.toString(index++), v);
		}
	}

	@Override
	protected final Wonton get(final String shallowKey) {
		try {
			return asArray().get(parseInt(shallowKey));
		} catch(final RuntimeException e) {
			throw new NoSuchPathException(e);
		}
	}

	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder("[");
		String delim = "";
		for(final Wonton v : asArray()) {
			builder.append(delim).append(v);
			delim = ",";
		}
		return builder.append("]").toString();
	}
}
