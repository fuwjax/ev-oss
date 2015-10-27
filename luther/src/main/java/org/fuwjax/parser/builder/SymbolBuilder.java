package org.fuwjax.parser.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;
import org.fuwjax.parser.impl.Symbol;

public class SymbolBuilder {
	public Symbol build() {
		if (symbol == null) {
			symbol = new Symbol(name, transform);
			symbol.init(start.build(), start.walk());
		}
		return symbol;
	}

	private Symbol symbol;
	private final String name;
	private final SymbolStateBuilder start;
	private boolean rightCycle;
	private Boolean nullable;
	private boolean checking;
	private final List<SymbolStateBuilder> states = new ArrayList<>();
	private final Function<Model, ? extends Node> transform;

	public SymbolBuilder(final String name, final Function<Model, ? extends Node> transform) {
		this.name = name;
		this.transform = transform;
		start = newState();
	}

	public SymbolStateBuilder start() {
		return start;
	}

	public boolean isRightCycle() {
		return rightCycle;
	}

	public boolean isNullable() {
		return nullable;
	}

	@Override
	public boolean equals(final Object obj) {
		try {
			final SymbolBuilder o = (SymbolBuilder) obj;
			return Objects.equals(name, o.name);
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public String toString() {
		return states().map(SymbolStateBuilder::toString).collect(Collectors.joining("\n"));
	}

	public Stream<SymbolStateBuilder> states() {
		return states.stream();
	}

	public Boolean checkNullable() {
		if (nullable == null) {
			if (checking) {
				return null;
			}
			checking = true;
			nullable = start.checkNullable();
			checking = false;
		}
		return nullable;
	}

	public void collapse() {
		for (int i = states.size() - 1; i >= 0; i--) {
			states.get(i).collapse();
		}
	}

	public boolean checkRightCycle() {
		if (checking) {
			return true;
		}
		checking = true;
		rightCycle = start.checkRightCycle();
		checking = false;
		return false;
	}

	public void checkRightRoot() {
		start.checkRightRoot();
	}

	public String name() {
		return name;
	}

	public Set<SymbolBuilder> buildPredict() {
		return start.buildPredict();
	}

	public SymbolStateBuilder newState() {
		final SymbolStateBuilder state = new SymbolStateBuilder(this, states.size());
		states.add(state);
		return state;
	}
}