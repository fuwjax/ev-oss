package org.echovantage.wonton;

import java.util.List;
import java.util.Map;

import org.echovantage.wonton.Wonton.Factory;
import org.echovantage.wonton.Wonton.FactoryType;
import org.echovantage.wonton.Wonton.Mutable;
import org.echovantage.wonton.Wonton.MutableFactory;
import org.echovantage.wonton.standard.BooleanWonton;
import org.echovantage.wonton.standard.ListWonton;
import org.echovantage.wonton.standard.MapWonton;
import org.echovantage.wonton.standard.NullWonton;
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
public enum StandardType implements FactoryType {
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
		public Wonton create(final Object value, final Factory factory) {
			assert value == null;
			return NullWonton.create(value, this);
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
		public Wonton create(final Object value, final Factory factory) {
			return BooleanWonton.create(value, this);
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
		public Wonton create(final Object value, final Factory factory) {
			return NumberWonton.create(value, this);
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
		public Wonton create(final Object value, final Factory factory) {
			return StringWonton.create(value, this);
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
		public Wonton create(final Object value, final Factory factory) {
			return ListWonton.create(value, this, factory);
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
		public Wonton create(final Object value, final Factory factory) {
			return MapWonton.create(value, this, factory);
		}
	};
	public static final MutableFactory FACTORY = new MutableFactory() {
		@Override
		public Wonton create(final Object value) {
			if(value instanceof Wonton) {
				return (Wonton) value;
			}
			for(final FactoryType type : values()) {
				try {
					final Wonton wonton = type.create(value, this);
					if(wonton != null) {
						return wonton;
					}
				} catch(final RuntimeException e) {
					// continue;
				}
			}
			return null;
		}

		@Override
		public Mutable createObject() {
			return new MapWonton(OBJECT);
		}

		@Override
		public Mutable createArray() {
			return new ListWonton(ARRAY);
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
