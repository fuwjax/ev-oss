package org.fuwjax.parser;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.fuwjax.parser.impl.MigratedModel;
import org.fuwjax.parser.impl.StandardModel;
import org.fuwjax.parser.impl.Symbol;
import org.fuwjax.parser.impl.Value;

public interface Model extends Node {
	Symbol symbol();

	Stream<Node> children();

	@Override
	default StringBuilder match(final StringBuilder builder) {
		children().forEach(node -> node.match(builder));
		return builder;
	}

	static Predicate<Model> named(final String... names) {
		final List<String> list = Arrays.asList(names);
		return model -> list.contains(model.symbol().name());
	}

	default Stream<Model> modelChildren() {
		return children().filter(Model.class::isInstance).map(Model.class::cast);
	}

	default Model get(final String... names) {
		return getAll(names).findFirst().orElse(null);
	}

	default Stream<Model> getAll(final String... names) {
		return modelChildren().filter(named(names));
	}

	default Object getValue(final String... names) {
		return getAllValues(names).findFirst().orElse(null);
	}

	default Stream<Object> getAllValues(final String... names) {
		return getAll(names).map(Model::value);
	}

	default Node node(final int index) {
		return children().skip(index).findFirst().map(Node::result).orElse(null);
	}

	static Function<Model, Node> wrap(final Function<Model, ?> transform) {
		return model -> {
			final Object value = transform.apply(model);
			if (value == model) {
				return new StandardModel(model);
			}
			if (!(value instanceof Model)) {
				if (value instanceof Node) {
					return (Node) value;
				}
				return new Value(model.symbol(), value);
			}
			final Model result = (Model) value;
			if (model.symbol().equals(result.symbol())) {
				return result;
			}
			return new MigratedModel(model.symbol(), result);
		};
	}
}
