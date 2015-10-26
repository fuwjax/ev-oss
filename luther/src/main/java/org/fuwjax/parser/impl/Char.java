package org.fuwjax.parser.impl;

import org.fuwjax.parser.Node;

public class Char implements Node {
	private final int ch;

	public Char(final int ch) {
		this.ch = ch;
	}

	@Override
	public StringBuilder match(final StringBuilder builder) {
		return builder.appendCodePoint(ch);
	}
	
	@Override
	public Integer value() {
		return ch;
	}
	
	@Override
	public Node result() {
		return this;
	}

	@Override
	public int hashCode() {
		return ch;
	}

	@Override
	public boolean equals(final Object obj) {
		try {
			final Char c = (Char) obj;
			return getClass().equals(c.getClass()) && c.ch == ch;
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return match();
	}
}