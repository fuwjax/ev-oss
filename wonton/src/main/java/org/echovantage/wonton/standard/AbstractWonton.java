package org.echovantage.wonton.standard;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.echovantage.wonton.Wonton;

public abstract class AbstractWonton implements Wonton {
	private final Type type;

	public AbstractWonton(final Type type) {
		assert type != null;
		this.type = type;
	}

	@Override
	public final Type type() {
		return type;
	}

	@Override
	public List<Wonton> asArray() {
		return null;
	}

	@Override
	public Boolean asBoolean() {
		return null;
	}

	@Override
	public Number asNumber() {
		return null;
	}

	@Override
	public Map<String, Wonton> asObject() {
		return null;
	}

	@Override
	public String asString() {
		return null;
	}

	@Override
	public Wonton get(final String key) {
		return null;
	}

	@Override
	public void accept(final Visitor visitor) {
		// do nothing
	}

	@Override
	public Object value() {
		return type().valueOf(this);
	}

	@Override
	public abstract String toString();

	@Override
	public boolean equals(final Object obj) {
		if(obj instanceof Wonton) {
			Wonton o = (Wonton) obj;
			return type().equals(o.type()) && Objects.equals(type().valueOf(this), type().valueOf(o));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type().valueOf(this));
	}
}
