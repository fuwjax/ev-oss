package org.fuwjax.parser.impl;

import java.util.Arrays;
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
	public Stream<Node> children() {
		return Stream.of(children);
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
		return "[" + state.name() + ", " + orig + "] " + match();
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

	public Boolean isBetterAlternative(final Transition next) {
		int c = children.length - next.children.length;
		if (c < 0) {
			return true;
		}
		if (c > 0) {
			return false;
		}
		c = state.compareTo(next.state);
		if (c < 0) {
			return false;
		}
		if (c > 0) {
			return true;
		}
		for (int i = 0; i < children.length; i++) {
			c = children[i].length() - next.children[i].length();
			if (c < 0) {
				return true;
			}
			if (c > 0) {
				return false;
			}
		}
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof Transition && next.children[i] instanceof Transition) {
				final Boolean result = ((Transition) children[i]).isBetterAlternative((Transition) next.children[i]);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
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
	public Node result() {
		final Node result = symbol().transform(this);
		return result == this ? new StandardModel(this) : result instanceof Transition ? result.result() : result;
	}

	@Override
	public Object value() {
		final Node result = result();
		return result == null ? null : result.value();
	}

	@Override
	public int length() {
		return children().mapToInt(Node::length).sum();
	}

	public void transformChildren() {
		for (int index = 0; index < children.length - 1; index++) {
			children[index] = children[index].result();
		}
	}
}