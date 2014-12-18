package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

import java.util.Map;

import static org.echovantage.wonton.standard.StringWonton.escape;

public abstract class AbstractMapWonton extends AbstractContainerWonton implements Wonton.WStruct {
	public static Wonton wrap(Map<String, ? extends Wonton> map){
		return new AbstractMapWonton() {
			@Override
			public Map<String, ? extends Wonton> asStruct() {
				return map;
			}
		};
	}

	@Override
	protected final void acceptShallow(final ShallowVisitor visitor) {
		for(final Map.Entry<String, ? extends Wonton> entry : asStruct().entrySet()) {
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
