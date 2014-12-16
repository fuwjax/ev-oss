package org.echovantage.wonton;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.echovantage.util.Lists;
import org.echovantage.util.ObjectMap;
import org.echovantage.util.ObjectMap.MapEntries;
import org.echovantage.wonton.standard.BooleanWonton;
import org.echovantage.wonton.standard.ListWrapper;
import org.echovantage.wonton.standard.MapWrapper;
import org.echovantage.wonton.standard.NullWonton;
import org.echovantage.wonton.standard.NumberWonton;
import org.echovantage.wonton.standard.StandardPath;
import org.echovantage.wonton.standard.StringWonton;

/**
 * The standard transport interface for Whatever Object NotaTiON. Wontons all
 * open this interface, but the implementation classes may vary wildly. It is
 * suggested to never instanceof with an implementation class, but to instead
 * rely on {@link #type()} for determining the Wonton type information.
 * @author fuwjax
 */
public interface Wonton {
	public static final Wonton NULL = new NullWonton();
	public static final Wonton TRUE = new BooleanWonton(true);
	public static final Wonton FALSE = new BooleanWonton(false);

	public static Path pathOf(final String... keys) {
		return StandardPath.pathOf(keys);
	}

	public static Path path(final String path) {
		return StandardPath.path(path);
	}

	public static Wonton wontonOf(final Object object) {
		if(object == null) {
			return NULL;
		}
		if(object instanceof Wonton) {
			return (Wonton) object;
		}
		if(object instanceof Boolean) {
			return (Boolean) object ? TRUE : FALSE;
		}
		if(object instanceof Number) {
			return new NumberWonton((Number) object);
		}
		if(object instanceof CharSequence) {
			return new StringWonton(object.toString());
		}
		if(object instanceof Map) {
			return new MapWrapper((Map<String, ?>) object);
		}
		if(object instanceof Iterable) {
			return new ListWrapper(Lists.toList((Iterable<?>) object));
		}
		if(object instanceof Object[]) {
			return new ListWrapper(Arrays.asList((Object[]) object));
		}
		if(object.getClass().isArray()) {
			return new ListWrapper(Lists.reflectiveList(object));
		}
		if(object.getClass().isAnnotationPresent(MapEntries.class)) {
			return new MapWrapper(ObjectMap.mapOf(object));
		}
		throw new IllegalArgumentException("No standard transformation for " + object.getClass());
	}

	public interface Path {
		String key();

		Path tail();

		Path append(Path suffix);

		boolean isEmpty();
	}

	/**
	 * The Visitor interface for {@link Wonton#accept(Visitor)}.
	 * @author fuwjax
	 */
	public interface Visitor {
		/**
		 * Visits a particular entry from the accepting wonton.
		 * @param path the entry path
		 * @param value the entry value
		 */
		public void visit(final Path path, final Wonton value);
	}

	public interface Mutable {
		Mutable set(Path path, Wonton value);

		default Mutable set(final String path, final Object value) {
			return set(path(path), wontonOf(value));
		}

		Mutable append(Path path, Wonton value);

		default Mutable append(final String path, final Object value) {
			return append(path(path), wontonOf(value));
		}

		Wonton build();
	}

	public interface Factory {
		Wonton wontonOf(Object value);
	}

	public enum Type {
		VOID(wonton -> null),
		BOOLEAN(Wonton::asBoolean),
		NUMBER(Wonton::asNumber),
		STRING(Wonton::asString),
		ARRAY(Wonton::asArray),
		STRUCT(Wonton::asStruct);
		private final Function<Wonton, Object> value;

		private Type(final Function<Wonton, Object> value) {
			this.value = value;
		}

		public Object valueOf(final Wonton wonton) {
			return value.apply(wonton);
		}
	}

	public class InvalidTypeException extends RuntimeException {
	}

	public class NoSuchPathException extends RuntimeException {
		public NoSuchPathException(final Path path) {
			super(path.toString());
		}

		public NoSuchPathException(final Throwable cause) {
			super(cause);
		}
	}

	default String asString() {
		throw new InvalidTypeException();
	}

	default Boolean asBoolean() {
		throw new InvalidTypeException();
	}

	default Number asNumber() {
		throw new InvalidTypeException();
	}

	default Map<String, ? extends Wonton> asStruct() {
		throw new InvalidTypeException();
	}

	default List<? extends Wonton> asArray() {
		throw new InvalidTypeException();
	}

	default Object value() {
		return type().valueOf(this);
	}

	Type type();

	default Wonton get(final Path path) {
		throw new NoSuchPathException(path);
	}

	default void accept(final Visitor visitor) {
		// do nothing
	}
}
