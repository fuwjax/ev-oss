package org.fuwjax.parser.impl;

import java.util.Objects;
import java.util.stream.Stream;

import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;

public class Value implements Model {
	private final Object value;
	private final Symbol symbol;

	public Value(final Symbol symbol, final Object value) {
		this.symbol = symbol;
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		try {
			final Value c = (Value) obj;
			return getClass().equals(c.getClass()) && Objects.equals(value, c.value);
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public StringBuilder match(final StringBuilder builder) {
		return builder.append(value);
	}

	public Symbol symbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return match();
	}

	@Override
	public Object value() {
		return value;
	}
	
	@Override
	public Node result() {
		return this;
	}

	@Override
	public Stream<Node> children() {
		return Stream.empty();
	}
}