package org.fuwjax.parser.impl;

import java.util.stream.Stream;

import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;

public class MigratedModel implements Model {
	private final Model model;
	private final Symbol symbol;

	public MigratedModel(final Symbol symbol, final Model model) {
		this.symbol = symbol;
		this.model = model;
	}

	@Override
	public StringBuilder match(final StringBuilder builder) {
		return model.match(builder);
	}

	@Override
	public Node result() {
		return this;
	}

	@Override
	public Object value() {
		return model.value();
	}

	@Override
	public int length() {
		return model.length();
	}

	@Override
	public Symbol symbol() {
		return symbol;
	}

	@Override
	public Stream<Node> children() {
		return model.children();
	}
}
