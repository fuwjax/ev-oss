package org.echovantage.wonton.standard;

import java.util.Objects;

import org.echovantage.wonton.Wonton;

public abstract class AbstractWonton implements Wonton {
	@Override
	public abstract String toString();

	@Override
	public final Object value() {
		return type().valueOf(this);
	}

	@Override
	public final boolean equals(final Object obj) {
		if(obj instanceof Wonton) {
			final Wonton o = (Wonton)obj;
			return type().equals(o.type()) && Objects.equals(id(this), id(o));
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(id(this));
	}

	protected Object id(final Wonton value) {
		return type().valueOf(value);
	}
}
