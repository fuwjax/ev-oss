package org.echovantage.wonton;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The standard transport interface for Whatever Object NotaTiON. Wontons all
 * open this interface, but the implementation classes may vary wildly. It is
 * suggested to never instanceof with an implementation class, but to instead
 * rely on {@link #type()} for determining the Wonton type information.
 * @author fuwjax
 */
public interface Wonton {
	/**
	 * The Visitor interface for {@link Wonton#accept(Visitor)}.
	 * @author fuwjax
	 */
	public interface Visitor {
		/**
		 * Visits a particular entry from the accepting wonton.
		 * @param key the entry key
		 * @param value the entry value
		 */
		public void visit(final String key, final Wonton value);
	}

	public interface MutableStruct {
		void set(String key, Wonton value);

		Wonton build();
	}

	public interface MutableArray extends MutableStruct {
		void append(Wonton value);
	}

	public interface Factory {
		MutableStruct newMutableStruct();

		MutableArray newMutableArray();

		Wonton wrap(Object value);
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

	public class NoSuchKeyException extends RuntimeException {
		public NoSuchKeyException() {
		}

		public NoSuchKeyException(final Throwable cause) {
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

	default Wonton get(final String key) {
		throw new NoSuchKeyException();
	}

	default void accept(final Visitor visitor) {
		// do nothing
	}

	@Override
	String toString();

	@Override
	boolean equals(Object obj);

	@Override
	public int hashCode();
}
