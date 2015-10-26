package org.fuwjax.parser;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.fuwjax.parser.impl.MigratedModel;
import org.fuwjax.parser.impl.Symbol;
import org.fuwjax.parser.impl.Value;

public interface Model extends Node {
	Symbol symbol();

	List<Node> children();

	@Override
	default StringBuilder match(final StringBuilder builder) {
		children().forEach(node -> node.match(builder));
		return builder;
	}

	static Predicate<Model> named(final String name) {
		return model -> model.symbol().name().equals(name);
	}

	default Stream<Model> modelChildren() {
		return children().stream().filter(Model.class::isInstance).map(Model.class::cast);
	}

	default Model get(final String name) {
		return getAll(name).findFirst().orElse(null);
	}

	default Stream<Model> getAll(final String name) {
		return modelChildren().filter(named(name));
	}

	default Object getValue(final String name) {
		return getAllValues(name).findFirst().orElse(null);
	}

	default Stream<Object> getAllValues(final String name) {
		return getAll(name).map(Model::value);
	}

	@Override
	default Model result() {
		final Object result = value();
		if (!(result instanceof Model)) {
			return new Value(symbol(), result);
		}
		final Model model = (Model) result;
		if (symbol().equals(model.symbol())) {
			return model;
		}
		return new MigratedModel(symbol(), model);
	}
}
