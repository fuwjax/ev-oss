package org.fuwjax.parser.impl;

import java.util.Objects;

public class Symbol {
	private final String name;
	private SymbolState start;
	private String toString;

	public Symbol(final String name) {
		this.name = name;
	}

	public void init(final SymbolState start, final String toString) {
		this.start = start;
		this.toString = toString;
	}

	public SymbolState start() {
		return start;
	}

	@Override
	public boolean equals(final Object obj) {
		try {
			final Symbol o = (Symbol) obj;
			return Objects.equals(name, o.name);
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public String toString() {
		return toString;
	}

	public String name() {
		return name;
	}
}