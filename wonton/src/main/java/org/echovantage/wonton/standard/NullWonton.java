package org.echovantage.wonton.standard;

import java.util.List;
import java.util.Map;

import org.echovantage.wonton.Wonton;

public class NullWonton extends AbstractWonton {
	public static Wonton NULL = new NullWonton();

	@Override
	public String toString() {
		return "null";
	}

	@Override
	public Type type() {
		return Type.VOID;
	}

	@Override
	public List<? extends Wonton> asArray() {
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
	public String asString() {
		return null;
	}

	@Override
	public Map<String, ? extends Wonton> asStruct() {
		return null;
	}
}
