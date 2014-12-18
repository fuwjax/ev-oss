package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

import java.util.Objects;

public abstract class AbstractWonton implements Wonton {
	@Override
	public abstract String toString();

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
		return value();
	}
}
