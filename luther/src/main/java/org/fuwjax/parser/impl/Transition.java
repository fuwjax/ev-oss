package org.fuwjax.parser.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;

public class Transition implements Model {
	private final SymbolState state;
	private final Origin orig;
	private final Node[] children;
	private final Transition insertion;
	private Transition alternative;

	public Transition(final Symbol s, final Origin orig) {
		this(s.start(), orig, new Node[0], null);
	}

	private Transition(final SymbolState state, final Origin orig, final Node[] children, final Transition insertion) {
		this.state = state;
		this.orig = orig;
		this.children = children;
		this.insertion = insertion;
	}

	@Override
	public Symbol symbol() {
		return state.lhs();
	}

	@Override
	public List<Node> children() {
		return Arrays.asList(children);
	}

	private Transition accept(final Transition child) {
		final SymbolState to = state.accept(child.symbol());
		return to == null ? null : append(to, child, insertion);
	}

	public Transition accept(final int a) {
		final SymbolState to = state.accept(a);
		return to == null ? null : append(to, new Char(a), insertion);
	}

	private Transition append(final Node node, final Transition insertionPoint) {
		return append(state, node, insertionPoint);
	}

	public Transition markOf(final Transition result) {
		return append(state, result, result);
	}

	private Transition append(final SymbolState to, final Node node, final Transition insertionPoint) {
		if (insertion == null) {
			final Node[] newChildren = Arrays.copyOf(children, children.length + 1);
			newChildren[children.length] = node;
			return new Transition(to, orig, newChildren, insertionPoint);
		}
		final Node[] newChildren = Arrays.copyOf(children, children.length);
		newChildren[children.length - 1] = ((Transition) newChildren[children.length - 1]).append(node, insertionPoint);
		return new Transition(to, orig, newChildren, insertionPoint);
	}

	public Stream<Symbol> predict() {
		return state.predict().stream();
	}

	public Stream<Symbol> pending() {
		return state.pending().stream();
	}

	public Symbol rightCycle() {
		return state.rightCycle();
	}

	@Override
	public String toString() {
		return "[" + state.lhs().name() + ", " + orig + "]";
	}

	@Override
	public boolean equals(final Object obj) {
		try {
			final Transition o = (Transition) obj;
			return state.equals(o.state) && orig.equals(o.orig);
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(state, orig);
	}

	public void addAlternative(final Transition next) {
		if (alternative == null) {
			alternative = next;
		} else {
			alternative.addAlternative(next);
		}
	}

	public void complete(final Consumer<Transition> parsePosition) {
		if (state.isComplete()) {
			orig.complete(mark -> parsePosition.accept(mark.accept(this)));
		}
	}

	public Transition mark() {
		return orig.markOf(this);
	}

	public boolean isModelFor(final Symbol symbol) {
		return symbol.equals(state.lhs()) && state.isComplete();
	}

	public void triggerTransform() {
		orig.triggerTransform();
	}

	@Override
	public Object value() {
		final Object result = state.result(this);
		return result == this ? new StandardModel(this) : result;
	}

	public void transformChildren() {
		for (int index = 0; index < children.length - 1; index++) {
			children[index] = children[index].result();
		}
	}
}