package org.echovantage.wonton.standard;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.echovantage.wonton.Wonton;

public abstract class AbstractWonton implements Wonton {
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
		return NULL;
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
	public int compareTo(final Wonton o) {
		return type().equals(o.type()) ? type().compare(this, o) : type().ordinal() - o.type().ordinal();
	}

	@Override
	public String toString() {
		return type().toString(this);
	}

	@Override
	public boolean equals(final Object obj) {
		if(obj instanceof Wonton) {
			return compareTo((Wonton)obj) == 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type().valueOf(this));
	}
}
