package org.fuwjax.parser.impl;

import java.io.IOException;

import org.fuwjax.oss.util.io.IntReader;
import org.fuwjax.parser.Grammar;
import org.fuwjax.parser.builder.SymbolBuilder;

public class GrammarImpl implements Grammar {
	private final Symbol accept;

	public GrammarImpl(final Symbol accept) {
		this.accept = accept;
	}

	public GrammarImpl(final SymbolBuilder start) {
		final SymbolBuilder accept = new SymbolBuilder("ACCEPT");
		accept.start().ensure("." + start.name(), start).complete(start.name() + ".",
				model -> model.get(start.name()));
		accept.checkNullable();
		accept.collapse();
		accept.buildPredict();
		accept.checkRightRoot();
		this.accept = accept.build();
	}

	@Override
	public Object parse(final IntReader input) throws IOException {
		return new ParseState().parse(accept, input);
	}

	@Override
	public String toString() {
		return accept.toString();
	}
}