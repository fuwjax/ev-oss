package org.fuwjax.parser.impl;

import java.util.Objects;
import java.util.function.Function;

import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;

public class Symbol {
	private final String name;
	private SymbolState start;
	private String toString;
	private final Function<Model, ? extends Node> transform;

	public Symbol(final String name, final Function<Model, ? extends Node> transform) {
		this.name = name;
		this.transform = transform;
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

	public Node transform(final Model model) {
		return transform.apply(model);
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