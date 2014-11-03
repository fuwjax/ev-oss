package org.echovantage.wonton;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.echovantage.wonton.Wonton.Factory;
import org.echovantage.wonton.standard.ListWonton;
import org.echovantage.wonton.standard.MapWonton;
import org.echovantage.wonton.standard.NumberWonton;
import org.echovantage.wonton.standard.StringWonton;

/**
 * The enum of possible Wonton types. Note in particular that there is no byte[]
 * type equivalent. Byte arrays tend to be serializations of richer objects,
 * which is what the OBJECT type is for. In those instances where a byte[] is
 * not a serialization of an object, e.g. image data, it is recommended to map
 * the bytes to characters, e.g. base64 encoding. When this is not desirable or
 * possible, consider using an alternate transport interface layer.
 * @author fuwjax
 */
public enum StandardType implements Wonton.Type {
	/**
	 * The null type. Nulls in Wonton do not represent a type without an
	 * instance, as in Java. They represent the absence of anything, including
	 * type information. As such, they can represent missing data, invalid keys,
	 * or literal nulls in the underlying data. Note that because they can
	 * represent a lack of anything, they do not (at least by contract) indicate
	 * what is lacking.
	 */
	NULL {
		@Override
		public Object valueOf(final Wonton wonton) {
			return null;
		}

		@Override
		public int compare(final Wonton o1, final Wonton o2) {
			return 0;
		}

		@Override
		public String toString(final Wonton wonton) {
			return "null";
		}

		@Override
		public Wonton create(final Object value, final Factory factory) {
			return value == null ? Wonton.NULL : null;
		}
	},
	/**
	 * The boolean type.
	 */
	BOOLEAN {
		@Override
		public Boolean valueOf(final Wonton wonton) {
			return wonton.asBoolean();
		}

		@Override
		public int compare(final Wonton o1, final Wonton o2) {
			return o1.asBoolean() ? o2.asBoolean() ? 0 : 1 : o2.asBoolean() ? -1 : 0;
		}

		@Override
		public String toString(final Wonton wonton) {
			return wonton.asBoolean() ? "true" : "false";
		}

		@Override
		public Wonton create(final Object value, final Factory factory) {
			return value instanceof Boolean ? (Boolean)value ? Wonton.TRUE : Wonton.FALSE : null;
		}
	},
	/**
	 * The number type. Note that in Java-ish, this represents any byte, double,
	 * float, int, long, and short. There is not a contractual way to determine
	 * what primitive type a value of this type could represent.
	 */
	NUMBER {
		@Override
		public Number valueOf(final Wonton wonton) {
			return wonton.asNumber();
		}

		@Override
		public int compare(final Wonton o1, final Wonton o2) {
			final double d1 = o1.asNumber().doubleValue();
			final double d2 = o2.asNumber().doubleValue();
			return d1 > d2 ? 1 : d1 == d2 ? 0 : -1;
		}

		@Override
		public String toString(final Wonton wonton) {
			return wonton.asNumber().toString();
		}

		@Override
		public Wonton create(final Object value, final Factory factory) {
			return value instanceof Number ? new NumberWonton((Number)value) : null;
		}
	},
	/**
	 * The string type.
	 */
	STRING {
		@Override
		public String valueOf(final Wonton wonton) {
			return wonton.asString();
		}

		@Override
		public int compare(final Wonton o1, final Wonton o2) {
			return o1.asString().compareTo(o2.asString());
		}

		@Override
		public String toString(final Wonton wonton) {
			return escape(wonton.asString());
		}

		@Override
		public Wonton create(final Object value, final Factory factory) {
			return value instanceof CharSequence ? new StringWonton(value.toString()) : null;
		}
	},
	/**
	 * The array type.
	 */
	ARRAY {
		@Override
		public List<? extends Wonton> valueOf(final Wonton wonton) {
			return wonton.asArray();
		}

		@Override
		public int compare(final Wonton o1, final Wonton o2) {
			final List<? extends Wonton> l1 = o1.asArray();
			final List<? extends Wonton> l2 = o2.asArray();
			for(int i = 0; i < l1.size() && i < l2.size(); i++) {
				final int c = l1.get(i).compareTo(l2.get(i));
				if(c != 0) {
					return c;
				}
			}
			return l1.size() - l2.size();
		}

		@Override
		public String toString(final Wonton wonton) {
			final StringBuilder builder = new StringBuilder("[");
			String delim = "";
			for(final Wonton v : wonton.asArray()) {
				builder.append(delim).append(v);
				delim = ",";
			}
			return builder.append("]").toString();
		}

		@Override
		public Wonton create(final Object array, final Factory factory) {
			ListWonton wonton = null;
			if(array != null) {
				if(array instanceof Iterable) {
					wonton = new ListWonton();
					for(final Object e : (Iterable<?>)array) {
						wonton.add(factory.create(e));
					}
				} else if(array instanceof Object[]) {
					wonton = new ListWonton();
					for(final Object e : (Object[])array) {
						wonton.add(factory.create(e));
					}
				} else if(array.getClass().isArray()) {
					wonton = new ListWonton();
					for(int i = 0; i < Array.getLength(array); i++) {
						wonton.add(factory.create(Array.get(array, i)));
					}
				}
			}
			return wonton;
		}
	},
	/**
	 * The object type.
	 */
	OBJECT {
		@Override
		public Map<String, ? extends Wonton> valueOf(final Wonton wonton) {
			return wonton.asObject();
		}

		@Override
		public int compare(final Wonton o1, final Wonton o2) {
			final Set<String> keys = new TreeSet<>();
			final Map<String, ? extends Wonton> m1 = o1.asObject();
			final Map<String, ? extends Wonton> m2 = o2.asObject();
			keys.addAll(m1.keySet());
			keys.addAll(m2.keySet());
			for(final String key : keys) {
				final int c = o1.get(key).compareTo(o2.get(key));
				if(c != 0) {
					return c;
				}
			}
			return 0;
		}

		@Override
		public String toString(final Wonton wonton) {
			final StringBuilder builder = new StringBuilder("{");
			String delim = "";
			for(final Map.Entry<String, ? extends Wonton> entry : wonton.asObject().entrySet()) {
				builder.append(delim).append(escape(entry.getKey())).append(":").append(entry.getValue());
				delim = ",";
			}
			return builder.append("}").toString();
		}

		@Override
		public Wonton create(final Object value, final Factory factory) {
			if(value instanceof Map) {
				final MapWonton wonton = new MapWonton();
				((Map<?, ?>)value).entrySet().forEach(entry -> wonton.put(String.valueOf(entry.getKey()), factory.create(entry.getValue())));
				return wonton;
			}
			return null;
		}
	};
	static String escape(final String value) {
		return '"' + value
				.replaceAll("\\\\", "\\\\")
				.replaceAll("/", "\\/")
				.replaceAll("\"", "\\\"")
				.replaceAll("\b", "\\b")
				.replaceAll("\f", "\\f")
				.replaceAll("\n", "\\n")
				.replaceAll("\r", "\\r")
				.replaceAll("\t", "\\t") + '"';
	}
}
