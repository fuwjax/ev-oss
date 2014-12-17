package org.echovantage.wonton;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;

public class WontonSerial {
	public static String toString(final Wonton result) {
		StringBuilder builder = new StringBuilder();
		try {
			new WontonSerial(builder).append(result);
		} catch(IOException e) {
			throw new RuntimeException("Impossible exception", e);
		}
		return builder.toString();
	}

	private final Appendable appender;

	public WontonSerial(final Appendable appender) {
		this.appender = appender;
	}

	public void append(final Wonton wonton) throws IOException {
		switch(wonton.type()) {
			case ARRAY:
				appendArray(wonton.asArray());
				break;
			case STRING:
				appendString(wonton.asString());
				break;
			case STRUCT:
				appendObject(wonton.asStruct());
				break;
			case NUMBER:
			case BOOLEAN:
			case VOID:
				appender.append(String.valueOf(wonton.value()));
				break;
			default:
				throw new IllegalArgumentException("No such type: " + wonton.type());
		}
	}

	private void appendObject(final Map<String, ? extends Wonton> struct) throws IOException {
		appender.append('{');
		boolean first = true;
		for(Map.Entry<String, ? extends Wonton> entry : struct.entrySet()) {
			if(first) {
				first = false;
			} else {
				appender.append(",");
			}
			appendString(entry.getKey());
			appender.append(":");
			append(entry.getValue());
		}
		appender.append('}');
	}

	private void appendString(final String string) throws IOException {
		appender.append('"');
		OfInt iter = string.codePoints().iterator();
		while(iter.hasNext()) {
			appendEncode(iter.nextInt());
		}
		appender.append('"');
	}

	private void appendEncode(final int cp) throws IOException {
		switch(cp) {
			case '\b':
				appender.append("\\b");
				return;
			case '\f':
				appender.append("\\f");
				return;
			case '\n':
				appender.append("\\n");
				return;
			case '\r':
				appender.append("\\r");
				return;
			case '\t':
				appender.append("\\t");
				return;
			case '\\':
				appender.append("\\\\");
				return;
			case '"':
				appender.append("\\\"");
				return;
			default:
				// continue
		}
		if(cp >= 0x20 && cp <= 0x7E) {
			appender.append((char) cp);
		} else if(cp <= 0xFFFF) {
			appender.append(hex(cp));
		} else {
			char[] chars = Character.toChars(cp);
			appender.append(hex(chars[0])).append(hex(chars[1]));
		}
	}

	private static String hex(final int cp) {
		return String.format("\\u%04X", cp);
	}

	private void appendArray(final List<? extends Wonton> array) throws IOException {
		appender.append('[');
		boolean first = true;
		for(Wonton wonton : array) {
			if(first) {
				first = false;
			} else {
				appender.append(",");
			}
			append(wonton);
		}
		appender.append(']');
	}
}
