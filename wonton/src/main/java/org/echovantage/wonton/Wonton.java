/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.echovantage.wonton;

import static org.echovantage.util.Charsets.UTF_8;
import static org.echovantage.util.MessageDigests.MD5;
import static org.echovantage.util.collection.Decorators.decorateList;
import static org.echovantage.util.collection.Decorators.decorateMap;
import static org.echovantage.util.function.Functions.function;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.echovantage.wonton.standard.BooleanWonton;
import org.echovantage.wonton.standard.NullWonton;
import org.echovantage.wonton.standard.RelaxedWonton;
import org.echovantage.wonton.standard.StandardPath;

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

	public static Path path(final String path) {
		return StandardPath.path(path);
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

	public enum Type {
		VOID(wonton -> null),
		BOOLEAN(Wonton::asBoolean),
		NUMBER(Wonton::asNumber),
		STRING(Wonton::asString),
		ARRAY(function(Wonton::asArray).andThen(list -> decorateList(list, Wonton::value))),
		STRUCT(function(Wonton::asStruct).andThen(map -> decorateMap(map, Wonton::value)));
		private final Function<Wonton, ?> value;

		private Type(final Function<Wonton, ?> value) {
			this.value = value;
		}

		public Object valueOf(final Wonton wonton) {
			return value.apply(wonton);
		}
	}

	public interface WVoid extends Wonton {
		@Override
		default Type type() {
			return Type.VOID;
		}

		@Override
		default String asString() {
			return null;
		}

		@Override
		default Boolean asBoolean() {
			return null;
		}

		@Override
		default List<? extends Wonton> asArray() {
			return null;
		}

		@Override
		default Map<String, ? extends Wonton> asStruct() {
			return null;
		}

		@Override
		default Number asNumber() {
			return null;
		}
	}

	public interface WString extends Wonton {
		@Override
		default Type type() {
			return Type.STRING;
		}

		@Override
		String asString();
	}

	public interface WBoolean extends Wonton {
		@Override
		default Type type() {
			return Type.BOOLEAN;
		}

		@Override
		Boolean asBoolean();
	}

	public interface WNumber extends Wonton {
		@Override
		default Type type() {
			return Type.NUMBER;
		}

		@Override
		Number asNumber();
	}

	public interface WStruct extends Wonton {
		@Override
		default Type type() {
			return Type.STRUCT;
		}

		@Override
		Map<String, ? extends Wonton> asStruct();

		@Override
		default Wonton get(final String key) {
			return asStruct().get(key);
		}

		@Override
		void accept(final Visitor visitor);
	}

	public interface WArray extends Wonton {
		@Override
		default Type type() {
			return Type.ARRAY;
		}

		@Override
		List<? extends Wonton> asArray();

		@Override
		default Wonton get(final String key) {
			try {
				return get(Integer.parseInt(key));
			} catch(final NumberFormatException e) {
				throw new NoSuchPathException(e);
			}
		}

		default Wonton get(final int index) {
			try {
				return asArray().get(index);
			} catch(final IndexOutOfBoundsException e) {
				throw new NoSuchPathException(e);
			}
		}

		@Override
		void accept(final Visitor visitor);
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

	default Integer asInteger() {
		return asNumber() == null ? null : asNumber().intValue();
	}

	default Byte asByte() {
		return asNumber() == null ? null : asNumber().byteValue();
	}

	default Short asShort() {
		return asNumber() == null ? null : asNumber().shortValue();
	}

	default Long asLong() {
		return asNumber() == null ? null : asNumber().longValue();
	}

	default Float asFloat() {
		return asNumber() == null ? null : asNumber().floatValue();
	}

	default Double asDouble() {
		return asNumber() == null ? null : asNumber().doubleValue();
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
		assert path != null;
		Wonton elm = this;
		for(Path p = path; !p.isEmpty(); p = p.tail()) {
			elm = elm.get(p.key());
			if(elm == null) {
				throw new NoSuchPathException(path);
			}
		}
		return elm;
	}

	default Wonton get(final String key) {
		return null;
	}

	default void accept(final Visitor visitor) {
		// do nothing
	}

	default Wonton relax() {
		return RelaxedWonton.relaxed(this);
	}

	default long tag() {
		final ByteBuffer buffer = ByteBuffer.wrap(MD5.digest(toString().getBytes(UTF_8)));
		return buffer.getLong() ^ buffer.getLong();
	}
}
