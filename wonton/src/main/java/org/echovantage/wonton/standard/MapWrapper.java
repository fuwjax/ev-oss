package org.echovantage.wonton.standard;

import java.util.Map;

import org.echovantage.util.MapDecorator;
import org.echovantage.wonton.Wonton;

public class MapWrapper extends AbstractMapWonton {
	private final Map<String, Wonton> map;

	public MapWrapper(final Map<String, ?> original) {
		assert original != null;
		map = new MapDecorator<>(original, Wonton::wontonOf);
	}

	@Override
	public Map<String, Wonton> asStruct() {
		return map;
	}
}
