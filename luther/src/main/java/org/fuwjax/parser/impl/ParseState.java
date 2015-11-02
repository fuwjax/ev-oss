package org.fuwjax.parser.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.stream.Collectors;

import org.fuwjax.oss.util.io.IntReader;

public class ParseState {
	private Map<Transition, Transition> items = new HashMap<>();
	private Map<Transition, Transition> oldItems = new HashMap<>();
	private final Set<Symbol> predict = new LinkedHashSet<>();
	private final Map<Symbol, Origin> origins = new HashMap<>();
	private int index;

	public Object parse(final Symbol accept, final IntReader input) {
		try {
			index = 0;
			final OfInt iter = input.stream().iterator();
			// can't just call save, as we need the predict set from accept
			add(new Transition(accept, origin(accept)));
			while (iter.hasNext()) {
				predict();
				System.out.println("****** Position: " + index);
				items.values().forEach(System.out::println);
				clear();
				acceptNext(iter.nextInt());
			}
			return result(accept);
		} catch (final Exception e) {
			throw e;
		}
	}

	private void acceptNext(final int ch) {
		index++;
		final List<Transition> consumers = oldItems.values().stream().map(item -> item.accept(ch)).filter(this::add)
				.collect(Collectors.toList());
		if (consumers.size() == 0) {
			throw new IllegalArgumentException(
					"Invalid input '" + new String(Character.toChars(ch)) + "' at position " + index);
		}
		if (consumers.size() == 1) {
			consumers.get(0).triggerTransform();
		}
	}

	private void clear() {
		final Map<Transition, Transition> temp = oldItems;
		oldItems = items;
		items = temp;
		items.clear();
		predict.clear();
		origins.clear();
	}

	private Origin origin(final Symbol symbol) {
		return origins.computeIfAbsent(symbol, s -> new Origin(index));
	}

	private boolean add(final Transition next) {
		System.out.println("adding @" + index + " " + next + (next == null ? ""
				: next.pending().map(Symbol::name).collect(Collectors.joining(", ", " awaiting ", ""))));
		if (next == null) {
			return false;
		}
		if (items.containsKey(next)) {
			// grammar is ambiguous
			final Transition current = items.get(next);
			final Boolean better = current.isBetterAlternative(next);
			System.out.println("Comparing (" + better + ") " + current.nestedString() + " to " + next.nestedString());
			if (better) {
				items.put(current, next);
			}
			return false;
		}
		save(next);
		next.complete(this::add);
		next.predict().filter(predict::add).forEach(this::save);
		return true;
	}

	private void save(final Symbol symbol) {
		System.out.println("predicting @" + index + " " + symbol.name() + symbol.start().pending().stream()
				.map(Symbol::name).collect(Collectors.joining(", ", " awaiting ", "")));
		save(new Transition(symbol, origin(symbol)));
	}

	private void save(final Transition item) {
		items.put(item, item);
		item.pending().forEach(pending -> origin(pending).addAwaiting(item));
	}

	private void predict() {
		for (final Transition item : items.values()) {
			final Symbol rr = item.rightCycle();
			if (rr != null) {
				origin(rr).setMark(item);
			}
		}
	}

	private Object result(final Symbol symbol) {
		return items.values().stream().filter(item -> item.isModelFor(symbol)).findAny()
				.orElseThrow(() -> new IllegalArgumentException("Invalid or incomplete input")).value();
	}
}