package org.echovantage.wonton;

import java.util.Arrays;

import org.echovantage.wonton.Wonton.Type;

public class StandardWontonFactory implements Wonton.Factory {
	public static final StandardWontonFactory FACTORY = new StandardWontonFactory(StandardType.values());
	private final Type[] types;

	public StandardWontonFactory(final Wonton.Type... types) {
		this.types = types;
	}

	@Override
	public Wonton create(final Object value) throws IllegalArgumentException {
		if(value instanceof Wonton) {
			return (Wonton)value;
		}
		for(final Type type : types) {
			try {
				final Wonton wonton = type.create(value, this);
				if(wonton != null) {
					return wonton;
				}
			} catch(final RuntimeException e) {
				// continue;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + Arrays.toString(types);
	}
}
