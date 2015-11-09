package org.fuwjax.parser.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fuwjax.oss.util.Iterables;
import org.fuwjax.oss.util.Lists;
import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;
import org.fuwjax.parser.impl.Symbol;

public class SymbolBuilder {
	public Symbol build() {
		if (symbol == null) {
			symbol = new Symbol(name, transform);
			symbol.init(start.build(), toString());
		}
		return symbol;
	}

	private Symbol symbol;
	private final String name;
	private final SymbolStateBuilder start;
	private Boolean nullable;
	private boolean checking;
	private final List<SymbolStateBuilder> states = new ArrayList<>();
	private final Function<Model, ? extends Node> transform;
	private Set<SymbolBuilder> rightSymbols;
	private boolean rightCycle;
	private SymbolBuilder ignore;

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
		Lists.reverse(new ArrayList<>(states)).forEach(SymbolStateBuilder::collapse);
		if (ignore != null) {
			for (int i = 0; i < states.size(); i++) {
				states.get(i).ignore(ignore);
			}
		}
		new ArrayList<>(states).forEach(state -> {
			Iterables.breakingForEach(states, other -> {
				if (state == other) {
					return false;
				}
				if (Objects.equals(state, other)) {
					states.stream().filter(Objects::nonNull).forEach(s -> s.replace(state, other));
					states.set(state.index(), null);
					return false;
				}
				return true;
			});
		});
		states.removeAll(Collections.singleton(null));
	}

	public Set<SymbolBuilder> rightSymbols() {
		if (rightSymbols == null) {
			rightSymbols = new HashSet<>();
			states.forEach(state -> state.rightSymbols().forEach(rightSymbols::add));
		}
		return rightSymbols;
	}

	public void checkRightRoot() {
		rightCycle = rightCycle();
		states.forEach(SymbolStateBuilder::checkRightRoot);
	}

	private boolean rightCycle() {
		final Set<SymbolBuilder> seen = new HashSet<>();
		Set<SymbolBuilder> set = rightSymbols();
		while (set.size() == 1) {
			if (set.contains(this)) {
				return true;
			}
			final SymbolBuilder tail = set.iterator().next();
			if (!seen.add(tail)) {
				return false;
			}
			set = tail.rightSymbols();
		}
		return false;
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

	public void setIgnore(final SymbolBuilder ignore) {
		this.ignore = ignore;
	}
}