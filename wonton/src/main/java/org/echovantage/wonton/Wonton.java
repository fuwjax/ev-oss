package org.echovantage.wonton;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.echovantage.wonton.standard.BooleanWonton;
import org.echovantage.wonton.standard.NullWonton;

/**
 * The standard transport interface for Whatever Object NotaTiON. Wontons all
 * open this interface, but the implementation classes may vary wildly. It is
 * suggested to never instanceof with an implementation class, but to instead
 * rely on {@link #type()} for determining the Wonton type information.
 * @author fuwjax
 */
public interface Wonton extends Comparable<Wonton> {
	/**
	 * Wonton representing null. This is not the only possible Wonton
	 * representing null, but null sentinel values should always equal this one.
	 */
	public static final Wonton NULL = NullWonton.NULL;
	/**
	 * Wonton representing true. This is not the only possible Wonton
	 * representing true, but true sentinel values should always equal this one.
	 */
	public static final Wonton TRUE = BooleanWonton.TRUE;
	/**
	 * Wonton representing false. This is not the only possible Wonton
	 * representing false, but false sentinel values should always equal this
	 * one.
	 */
	public static final Wonton FALSE = BooleanWonton.FALSE;

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

	/**
	 * Creates a wonton from a source object.
	 * @author fuwjax
	 */
	public interface Factory {
		Wonton create(Object value);
	}

	public interface Type extends Comparator<Wonton> {
		/**
		 * Returns the value of the wonton as though it were an instance of this
		 * type. There is no type checking performed.
		 * @param wonton the wonton to value
		 * @return the value of the wonton
		 */
		Object valueOf(Wonton wonton);

		/**
		 * Compares the values of two wontons for order, wihtout respect for the
		 * type of either wonton. The values are the same {@link #valueOf(Wonton)}
		 * would return.
		 */
		@Override
		int compare(Wonton o1, Wonton o2);

		/**
		 * Returns the json equivalent of the wonton according to this type.
		 * @param wonton the wonton to jsonify
		 * @return the json string representing this wonton as seen by this type
		 */
		String toString(Wonton wonton);

		/**
		 * The types returned by a WontonFactory need a natural ordering.
		 * @return the natural index of this type within a factory
		 */
		int ordinal();

		Wonton create(Object value, Factory factory);
	}

	String asString();

	Boolean asBoolean();

	Number asNumber();

	Map<String, ? extends Wonton> asObject();

	List<? extends Wonton> asArray();

	Object value();

	Type type();

	Wonton get(String key);

	void accept(Visitor visitor);
}
