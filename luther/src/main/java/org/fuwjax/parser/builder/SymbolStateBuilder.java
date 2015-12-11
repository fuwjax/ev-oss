package org.fuwjax.parser.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	private boolean canMatch() {
		return !this.complete || !symbolic.isEmpty() || !literals.isEmpty();
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
		if (predict != null) {
			predict.stream().forEach(e -> builder.append(e.name()).append(" "));
		}
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

	public Stream<SymbolBuilder> rightSymbols() {
		return symbolic.entrySet().stream().filter(e -> e.getValue().complete).map(Map.Entry::getKey);
	}

	public void checkRightRoot() {
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

	public String name() {
		return lhs.name() + "." + index;
	}

	public int index() {
		return index;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		try {
			final SymbolStateBuilder o = (SymbolStateBuilder) obj;
			return o.complete == complete && symbolic.equals(o.symbolic) && literals.equals(o.literals);
		} catch (final Exception e) {
			return false;
		}
	}

	public void replace(final SymbolStateBuilder match, final SymbolStateBuilder replace) {
		literals.entrySet().stream().filter(e -> e.getValue().index == match.index).forEach(e -> e.setValue(replace));
		symbolic.entrySet().stream().filter(e -> e.getValue().index == match.index).forEach(e -> e.setValue(replace));
	}

	public void ignore(final SymbolBuilder ignore) {
		symbolic.entrySet().stream().filter(e -> !e.getKey().equals(ignore)).map(Entry::getValue)
				.filter(SymbolStateBuilder::canMatch).forEach(sym -> sym.addIgnore(ignore, true));
		literals.values().forEach(lit -> lit.addIgnore(ignore, false));
	}

	private void addIgnore(final SymbolBuilder ignore, final boolean insert) {
		final SymbolStateBuilder ignoreState = ensure(name(), ignore);
		for (final Entry<Codepoints, SymbolStateBuilder> entry : literals.entrySet()) {
			ignoreState.ensure(entry.getKey()).merge(entry.getValue());
		}
		for (final Iterator<Map.Entry<SymbolBuilder, SymbolStateBuilder>> iter = symbolic.entrySet().iterator(); iter
				.hasNext();) {
			final Entry<SymbolBuilder, SymbolStateBuilder> entry = iter.next();
			if (!ignore.equals(entry.getKey())) {
				ignoreState.ensure(entry.getKey()).merge(entry.getValue());
				if (insert) {
					iter.remove();
				}
			}
		}
		ignoreState.names.addAll(names);
	}
}