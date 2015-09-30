/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
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
package org.fuwjax.oss.util.parser;

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
		public synchronized Throwable getCause() {
			return super.getCause() instanceof ParseException ? null : super.getCause();
		}

		@Override
		public synchronized Throwable fillInStackTrace() {
			return this;
		}

		@Override
		public String getMessage() {
			if(super.getCause() instanceof ParseException) {
				return super.getCause().getMessage() + "\n" + super.getMessage();
			}
			return super.getMessage();
		}
	}
}
