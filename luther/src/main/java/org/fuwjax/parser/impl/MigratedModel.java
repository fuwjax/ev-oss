package org.fuwjax.parser.impl;

import java.util.stream.Stream;

import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;

public class MigratedModel implements Model {
	private Model model;
	private Symbol symbol;

	public MigratedModel(Symbol symbol, Model model) {
		this.symbol = symbol;
		this.model = model;
	}

	@Override
	public StringBuilder match(StringBuilder builder) {
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
	public Symbol symbol() {
		return symbol;
	}

	@Override
	public Stream<Node> children() {
		return model.children();
	}
}
