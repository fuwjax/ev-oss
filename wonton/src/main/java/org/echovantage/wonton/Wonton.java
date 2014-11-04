package org.echovantage.wonton;

import java.util.List;
import java.util.Map;

/**
 * The standard transport interface for Whatever Object NotaTiON. Wontons all
 * open this interface, but the implementation classes may vary wildly. It is
 * suggested to never instanceof with an implementation class, but to instead
 * rely on {@link #type()} for determining the Wonton type information.
 * @author fuwjax
 */
public interface Wonton {
	public interface Mutable {
		void set(String key, Wonton value);

		Wonton build();
	}

	public interface MutableFactory extends Factory {
		Mutable createObject();

		Mutable createArray();
	}

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

	public interface Type {
		/**
		 * Returns the value of the wonton as though it were an instance of this
		 * type. There is no type checking performed.
		 * @param wonton the wonton to value
		 * @return the value of the wonton
		 */
		Object valueOf(Wonton wonton);
	}

	public interface FactoryType extends Type {
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
