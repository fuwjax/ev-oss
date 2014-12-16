package org.echovantage.util.parser;

public class ParseState {
	private final Integer codepoint;
	private final long line;
	private final long column;
	private final long offset;

	public ParseState(final Integer codepoint, final long line, final long column, final long offset) {
		this.codepoint = codepoint;
		this.line = line;
		this.column = column;
		this.offset = offset;
	}

	public ParseException fail(final String message, final Throwable cause) {
		return new ParseException(message, cause);
	}

	public ParseException fail(final String message) {
		return new ParseException(message);
	}

	public ParseException fail(final int expected) {
		return new ParseException(expected);
	}

	String message(final String message) {
		if(codepoint == null) {
			return String.format("%s at line %d column %d", message, line, column);
		}
		return String.format("%s, was %s at line %d column %d", message, quote(codepoint), line, column);
	}

	private static String quote(final int cp) {
		return cp == -1 ? "EOF" : "'" + new String(Character.toChars(cp)) + "'";
	}

	private class ParseException extends java.text.ParseException {
		private static final long serialVersionUID = 1L;

		ParseException(final int expected) {
			this("Expected " + quote(expected));
		}

		ParseException(final String message) {
			super(message(message), (int) offset);
		}

		ParseException(final String message, final Throwable cause) {
			this(message);
			initCause(cause);
		}

		@Override
		public synchronized Throwable fillInStackTrace() {
			return this;
		}

		@Override
		public String getMessage() {
			if(getCause() != null) {
				return getCause().getMessage() + "\n" + super.getMessage();
			}
			return super.getMessage();
		}
	}
}
