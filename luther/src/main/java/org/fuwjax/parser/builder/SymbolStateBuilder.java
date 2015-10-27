package org.fuwjax.parser.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

import org.fuwjax.parser.impl.SymbolState;

public class SymbolStateBuilder {
	private final SymbolBuilder lhs;
	private final Set<String> names = new HashSet<>();
	private final Map<SymbolBuilder, SymbolStateBuilder> symbolic = new HashMap<>();
	private final Map<Codepoints, SymbolStateBuilder> literals = new HashMap<>();
	private Set<SymbolBuilder> predict;
	private SymbolBuilder rightCycle;
	private boolean complete;
	private SymbolState state;
	private final int index;

	SymbolStateBuilder(final SymbolBuilder lhs, final int index) {
		this.lhs = lhs;
		this.index = index;
	}

	public SymbolStateBuilder ensure(final String name, final SymbolBuilder accept) {
		names.add(name);
		return ensure(accept);
	}

	private SymbolStateBuilder ensure(final SymbolBuilder accept) {
		SymbolStateBuilder s = symbolic.get(accept);
		if (s == null) {
			s = lhs.newState();
			symbolic.put(accept, s);
		}
		return s;
	}

	public SymbolStateBuilder ensure(final String name, final Codepoints literalMask) {
		names.add(name);
		return ensure(literalMask);
	}

	private SymbolStateBuilder ensure(final Codepoints literalMask) {
		SymbolStateBuilder s = literals.get(literalMask);
		if (s == null) {
			// TODO: check for intersection
			s = lhs.newState();
			literals.put(literalMask, s);
		}
		return s;
	}

	public void complete(final String name) {
		names.add(name);
		this.complete = true;
	}

	public String walk() {
		final StringBuilder builder = new StringBuilder();
		builder.append(this);
		for (final SymbolStateBuilder s : literals.values()) {
			builder.append("\n").append(s.walk());
		}
		for (final SymbolStateBuilder s : symbolic.values()) {
			builder.append("\n").append(s.walk());
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(name()).append(": ");
		if (complete) {
			builder.append("! ");
		}
		symbolic.entrySet().forEach(
				e -> builder.append(e.getKey().name()).append(" -> ").append(e.getValue().name()).append("  "));
		literals.entrySet()
				.forEach(e -> builder.append(e.getKey()).append(" -> ").append(e.getValue().name()).append("  "));
		builder.append(" :: ");
		predict.stream().forEach(e -> builder.append(e.name()).append(" "));
		;
		return builder.toString();
	}

	private void merge(final SymbolStateBuilder state) {
		for (final Map.Entry<Codepoints, SymbolStateBuilder> entry : state.literals.entrySet()) {
			ensure(entry.getKey()).merge(entry.getValue());
		}
		for (final Map.Entry<SymbolBuilder, SymbolStateBuilder> entry : state.symbolic.entrySet()) {
			ensure(entry.getKey()).merge(entry.getValue());
		}
		names.addAll(state.names);
		complete |= state.complete;
	}

	public boolean checkNullable() {
		if (complete) {
			return true;
		}
		for (final Map.Entry<SymbolBuilder, SymbolStateBuilder> entry : symbolic.entrySet()) {
			if (entry.getValue().checkNullable() && entry.getKey().checkNullable()) {
				return true;
			}
		}
		return false;
	}

	public void collapse() {
		final Set<SymbolStateBuilder> states = symbolic.entrySet().stream().filter(e -> e.getKey().isNullable())
				.map(Map.Entry::getValue).collect(Collectors.toSet());
		// Collect to avoid a ConcurrentModificationException
		states.forEach(this::merge);
	}

	public Set<SymbolBuilder> buildPredict() {
		if (predict != null) {
			return predict;
		}
		if (symbolic.isEmpty()) {
			predict = Collections.emptySet();
		} else {
			predict = new HashSet<>(symbolic.keySet());
			for (final SymbolBuilder s : symbolic.keySet()) {
				predict.addAll(s.buildPredict());
			}
		}
		for (final SymbolStateBuilder s : symbolic.values()) {
			s.buildPredict();
		}
		for (final SymbolStateBuilder s : literals.values()) {
			s.buildPredict();
		}
		return predict;
	}

	public boolean checkRightCycle() {
		for (final SymbolStateBuilder s : literals.values()) {
			if (s.checkRightCycle()) {
				return true;
			}
		}
		for (final Map.Entry<SymbolBuilder, SymbolStateBuilder> entry : symbolic.entrySet()) {
			if (entry.getValue().checkRightCycle()) {
				return true;
			}
			if (entry.getValue().complete && entry.getKey().checkRightCycle()) {
				return true;
			}
		}
		return false;
	}

	public void checkRightRoot() {
		for (final SymbolStateBuilder s : literals.values()) {
			s.checkRightRoot();
		}
		for (final Map.Entry<SymbolBuilder, SymbolStateBuilder> entry : symbolic.entrySet()) {
			entry.getValue().checkRightRoot();
		}
		if (symbolic.size() == 1 && literals.isEmpty()) {
			for (final Map.Entry<SymbolBuilder, SymbolStateBuilder> entry : symbolic.entrySet()) {
				if (entry.getKey().isRightCycle() && entry.getValue().complete) {
					rightCycle = entry.getKey();
				}
			}
		}
	}

	public SymbolState build() {
		if (state == null) {
			state = new SymbolState();
			state.init(lhs.build(), index,
					symbolic.entrySet().stream()
							.collect(Collectors.toMap(e -> e.getKey().build(), e -> e.getValue().build())),
					literalFunction(), predict.stream().map(SymbolBuilder::build).collect(Collectors.toSet()),
					rightCycle == null ? null : rightCycle.build(), complete, toString());
		}
		return state;
	}

	private IntFunction<SymbolState> literalFunction() {
		final Map<IntPredicate, SymbolState> states = literals.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().toPredicate(), e -> e.getValue().build()));
		return ch -> {
			for (final Map.Entry<IntPredicate, SymbolState> entry : states.entrySet()) {
				if (entry.getKey().test(ch)) {
					return entry.getValue();
				}
			}
			return null;
		};
	}

	public void states(final Set<SymbolStateBuilder> states) {
		if (states.add(this)) {
			symbolic.values().forEach(s -> s.states(states));
			literals.values().forEach(s -> s.states(states));
		}
	}

	public String name() {
		return lhs.name() + "." + index;
	}
}