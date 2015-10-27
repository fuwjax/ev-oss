package org.fuwjax.parser.builder;

import static java.util.function.Function.identity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.fuwjax.parser.Grammar;
import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;
import org.fuwjax.parser.impl.GrammarImpl;

public class GrammarBuilder {
	private final Map<String, SymbolBuilder> symbols = new HashMap<>();
	private final Map<String, Function<Model, ? extends Node>> transforms;

	public GrammarBuilder() {
		this(new HashMap<>());
	}

	public GrammarBuilder(final Map<String, Function<Model, ? extends Node>> transforms) {
		this.transforms = transforms;
	}
	
	public void transform(String name, Function<Model, ?> transform){
		transforms.put(name, Model.wrap(transform));
	}

	public Function<Model, ? extends Node> transform(final String name) {
		return transforms.getOrDefault(name, identity());
	}

	public SymbolBuilder symbol(final String name) {
		return symbols.computeIfAbsent(name, key -> new SymbolBuilder(key, transform(key)));
	}

	protected Stream<SymbolBuilder> symbols() {
		return symbols.values().stream();
	}

	public Grammar build(final String start) {
		symbols().forEach(SymbolBuilder::checkNullable);
		symbols().forEach(SymbolBuilder::collapse);
		symbols().forEach(SymbolBuilder::buildPredict);
		symbols().forEach(SymbolBuilder::checkRightRoot);
		symbols().forEach(System.out::println);
		return new GrammarImpl(symbol(start));
	}
}