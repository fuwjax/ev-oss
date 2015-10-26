package org.fuwjax.parser.builder;

import static java.util.function.Function.identity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.fuwjax.parser.Grammar;
import org.fuwjax.parser.Model;
import org.fuwjax.parser.impl.GrammarImpl;

public class GrammarBuilder {
	private final Map<String, SymbolBuilder> symbols = new HashMap<>();
	private final Map<String, Function<Model, ?>> transforms;

	public GrammarBuilder() {
		this(Collections.emptyMap());
	}

	public GrammarBuilder(final Map<String, Function<Model, ?>> transforms) {
		this.transforms = transforms;
	}

	public Function<Model, ?> transform(final String name) {
		return transforms.getOrDefault(name, identity());
	}

	public SymbolBuilder symbol(final String name) {
		return symbols.computeIfAbsent(name, SymbolBuilder::new);
	}

	public Grammar build(final String start) {
		for (final SymbolBuilder s : symbols.values()) {
			s.checkNullable();
		}
		for (final SymbolBuilder s : symbols.values()) {
			s.collapse();
		}
		for (final SymbolBuilder s : symbols.values()) {
			s.buildPredict();
		}
		for (final SymbolBuilder s : symbols.values()) {
			s.checkRightCycle();
		}
		for (final SymbolBuilder s : symbols.values()) {
			s.checkRightRoot();
		}
		return new GrammarImpl(symbol(start));
	}
}