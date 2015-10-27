package org.fuwjax.parser.bnf;

import org.fuwjax.parser.bnf.BnfGrammar.Builder;

public class Reference implements Expression {
	private final String name;

	public Reference(final String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	@Override
	public Object toStep(final Builder builder) {
		return builder.symbol(name);
	}

	@Override
	public String toString() {
		return "Ref[" + name + "]";
	}
}
