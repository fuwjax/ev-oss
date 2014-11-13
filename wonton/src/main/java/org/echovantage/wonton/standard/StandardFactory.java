package org.echovantage.wonton.standard;

import java.util.Arrays;
import java.util.Map;

import org.echovantage.util.Lists;
import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.Wonton.MutableArray;
import org.echovantage.wonton.Wonton.MutableStruct;

public class StandardFactory {
	public static final StandardFactory FACTORY = new StandardFactory();
	public static final Wonton NULL = new NullWonton();
	public static final Wonton TRUE = new BooleanWonton(true);
	public static final Wonton FALSE = new BooleanWonton(false);

	public Wonton wrap(final Object object) {
		if(object == null) {
			return NULL;
		}
		if(object instanceof Wonton) {
			return (Wonton)object;
		}
		if(object instanceof Boolean) {
			return (Boolean)object ? TRUE : FALSE;
		}
		if(object instanceof Number) {
			return new NumberWonton((Number)object);
		}
		if(object instanceof CharSequence) {
			return new StringWonton(object.toString());
		}
		if(object instanceof Map) {
			return new MapWrapper((Map<String, ?>)object);
		}
		if(object instanceof Iterable) {
			return new ListWrapper(Lists.toList((Iterable<?>)object));
		}
		if(object instanceof Object[]) {
			return new ListWrapper(Arrays.asList((Object[])object));
		}
		if(object.getClass().isArray()) {
			return new ListWrapper(Lists.reflectiveList(object));
		}
		throw new IllegalArgumentException("No standard transformation for " + object.getClass());
	}

	public MutableStruct newMutableStruct() {
		return new MapWonton();
	}

	public MutableArray newMutableArray() {
		return new ListWonton();
	}
}
