package org.fuwjax.parser;

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

	static Predicate<Model> named(final String name) {
		return model -> model.symbol().name().equals(name);
	}

	default Stream<Model> modelChildren() {
		return children().filter(Model.class::isInstance).map(Model.class::cast);
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
	
	static Function<Model, Node> wrap(Function<Model, ?> transform){
		return model -> {
			Object value = transform.apply(model);
			if(value == model){
				return new StandardModel(model);
			}
			if (!(value instanceof Model)) {
				if(value instanceof Node){
					return (Node)value;
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
