package org.fuwjax.oss.lumber;

import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Json {
	private static final String WS = " \n\r\t";
	private static final String RESERVED = WS + "{}[],:\"";
	private final List<? extends Normalizer> normalizers;

	public interface Normalizer {
		Object normalize(Object obj);

		boolean canNormalize(Class<?> cls);
	}

	public Json(final List<? extends Normalizer> normalizers) {
		this.normalizers = normalizers;
	}

	public Json(final Normalizer... normalizers) {
		this(Arrays.asList(normalizers));
	}

	public CharSequence toString(final Object obj) {
		if (obj == null) {
			return "null";
		}
		final Object converted = normalizers.stream().filter(s -> s.canNormalize(obj.getClass())).findFirst()
				.map(c -> c.normalize(obj)).orElse(obj);
		if (converted instanceof Number || converted instanceof Boolean) {
			return String.valueOf(converted);
		}
		if (converted instanceof String) {
			return new StringBuilder().append('\"').append(converted.toString().replaceAll("\\\\", "\\\\")
					.replaceAll("\n", "\\n").replaceAll("\t", "\\t").replaceAll("\r", "\\r").replaceAll("\"", "\\\""))
					.append('\"');
		}
		if (converted.getClass().isArray() || converted instanceof Iterable) {
			return "[" + streamOf(converted).map(this::toString).collect(joining(",")) + "]";
		}
		if (converted instanceof Map) {
			return "{" + ((Map<?, ?>) converted).entrySet().stream()
					.map(e -> toString(String.valueOf(e.getKey())) + ":" + toString(e.getValue())).collect(joining(","))
					+ "}";
		}
		return toString(String.valueOf(converted));
	}

	public Object toObject(final CharSequence input) {
		final Object[] result = new Object[1];
		parseValue(' ', input.codePoints().iterator(), o -> result[0] = o);
		return result[0];
	}

	private static int parseValue(final int start, final OfInt codePoints, final Consumer<Object> handler) {
		final int cp = skipWS(start, codePoints);
		switch (cp) {
		case -1:
			throw new IllegalStateException("Unexpected EOF");
		case '{':
			return parseObject(codePoints, handler);
		case '[':
			return parseArray(codePoints, handler);
		case '"':
			return parseString(codePoints, handler);
		case ']':
		case '}':
		case ':':
		case ',':
			throw new IllegalStateException("Expected to start a value, but found '" + (char) cp + "'");
		default:
			return parseLiteralValue(cp, codePoints, handler);
		}
	}

	private static int parseObject(final OfInt codePoints, final Consumer<Object> handler) {
		final Map<String, Object> map = new LinkedHashMap<>();
		int cp = skipWS(' ', codePoints);
		while (cp != '}') {
			final Object[] key = new Object[1];
			cp = parseValue(cp, codePoints, o -> key[0] = o);
			cp = skipWS(cp, codePoints);
			expect(cp, ':');
			cp = parseValue(' ', codePoints, o -> map.put(String.valueOf(key[0]), o));
			cp = skipWS(cp, codePoints);
			if (cp == ',') {
				cp = skipWS(cp, codePoints);
			} else {
				expect(cp, '}');
			}
		}
		handler.accept(map);
		return next(codePoints);
	}

	private static int next(final OfInt codePoints) {
		return codePoints.hasNext() ? codePoints.nextInt() : -1;
	}

	private static void expect(final int cp, final int expected) {
		if (cp != expected) {
			throw new IllegalStateException("Invalid Json syntax, expected '" + (char) expected + "', but found '"
					+ new String(Character.toChars(cp)) + "'");
		}
	}

	private static int parseArray(final OfInt codePoints, final Consumer<Object> handler) {
		final List<Object> list = new ArrayList<>();
		int cp = skipWS(' ', codePoints);
		while (cp != ']') {
			cp = parseValue(cp, codePoints, list::add);
			cp = skipWS(cp, codePoints);
			if (cp == ',') {
				cp = skipWS(cp, codePoints);
			} else {
				expect(cp, ']');
			}
		}
		handler.accept(list);
		return next(codePoints);
	}

	private static int parseString(final OfInt codePoints, final Consumer<Object> handler) {
		final StringBuilder buffer = new StringBuilder();
		int cp = next(codePoints);
		while (cp != '"') {
			if (cp == '\\') {
				cp = next(codePoints);
				switch (cp) {
				case 'b':
					cp = '\b';
					break;
				case 'f':
					cp = '\f';
					break;
				case 'n':
					cp = '\n';
					break;
				case 'r':
					cp = '\r';
					break;
				case 't':
					cp = '\t';
					break;
				case 'u':
					final StringBuilder b = new StringBuilder();
					b.appendCodePoint(next(codePoints));
					b.appendCodePoint(next(codePoints));
					b.appendCodePoint(next(codePoints));
					b.appendCodePoint(next(codePoints));
					cp = Integer.parseInt(b.toString(), 16);
					break;
				default:
					// cp = cp;
				}
			}
			buffer.appendCodePoint(cp);
			cp = next(codePoints);
		}
		handler.accept(buffer.toString());
		return next(codePoints);
	}

	private static int skipWS(final int start, final OfInt codePoints) {
		int cp = start;
		while (WS.indexOf(cp) >= 0) {
			cp = next(codePoints);
		}
		return cp;
	}

	private static int parseLiteralValue(final int start, final OfInt codePoints, final Consumer<Object> handler) {
		final StringBuilder buffer = new StringBuilder();
		int cp = start;
		do {
			buffer.appendCodePoint(cp);
			cp = next(codePoints);
		} while (RESERVED.indexOf(cp) < 0);
		final String literal = buffer.toString();
		switch (literal) {
		case "null":
			handler.accept(null);
			break;
		case "true":
			handler.accept(true);
			break;
		case "false":
			handler.accept(false);
			break;
		default:
			try {
				handler.accept(numberOf(literal));
			} catch (final NumberFormatException e) {
				handler.accept(literal);
			}
		}
		return cp;
	}

	private static Number numberOf(final String literal) {
		try {
			return Integer.valueOf(literal);
		} catch (final NumberFormatException e) {
			try {
				return Long.valueOf(literal);
			} catch (final NumberFormatException ex) {
				return Double.valueOf(literal);
			}
		}
	}

	private static Stream<?> streamOf(final Object obj) {
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).stream();
		}
		Iterable<?> iter;
		if (obj instanceof Iterable) {
			iter = (Iterable<?>) obj;
		} else {
			iter = iterableOf(obj);
		}
		return stream(iter.spliterator(), false);
	}

	private static Iterable<?> iterableOf(final Object obj) {
		return () -> new Iterator<Object>() {
			int index = 0;

			@Override
			public boolean hasNext() {
				return index < Array.getLength(obj);
			}

			@Override
			public Object next() {
				return Array.get(obj, index++);
			}
		};
	}

}
