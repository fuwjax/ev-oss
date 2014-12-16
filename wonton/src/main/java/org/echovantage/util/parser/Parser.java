package org.echovantage.util.parser;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.IntPredicate;

import org.echovantage.util.Strings;

public class Parser {
	private long line = 1;
	private long column;
	private long offset;
	private int cp;
	private boolean ready;
	private final IntReader stream;

	public Parser(final IntReader stream) {
		this.stream = stream;
	}

	public Parser skip(final IntPredicate skip) throws IOException {
		while(skip.test(peek())) {
			ready = false;
		}
		return this;
	}

	public ParseState expect(final int codepoint) throws IOException, ParseException {
		int c = peek();
		ParseState state = state();
		if(c != codepoint) {
			throw state.fail(codepoint);
		}
		ready = false;
		return state;
	}

	public boolean isa(final int codepoint) throws IOException {
		if(peek() == codepoint) {
			ready = false;
		}
		return !ready;
	}

	public int read() throws IOException, ParseException {
		int c = peek();
		if(c == -1) {
			throw state().fail("Unexpected end of stream");
		}
		ready = false;
		return c;
	}

	public int peek() throws IOException {
		if(!ready) {
			ready = true;
			cp = stream.read();
			offset++;
			if(cp == '\n') {
				line++;
				column = 0;
			} else {
				column++;
			}
		}
		return cp;
	}

	public ParseException fail(final String message) {
		return state().fail(message);
	}

	@Override
	public String toString() {
		return state().message("From \u2026" + Strings.ellipse(stream.toString(), 20));
	}

	public ParseState state() {
		return new ParseState(ready ? cp : null, line, column, offset);
	}
}
