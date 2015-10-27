package org.fuwjax.parser.impl;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;

public class SymbolState implements Comparable<SymbolState> {
	private Symbol lhs;
	private String toString;
	private Map<Symbol, SymbolState> symbolic;
	private IntFunction<SymbolState> literals;
	private Set<Symbol> predict;
	private Symbol rightCycle;
	private int index;
	private boolean complete;

	public void init(final Symbol lhs, final int index, final Map<Symbol, SymbolState> symbolic,
			final IntFunction<SymbolState> literals, final Set<Symbol> predict, final Symbol rightCycle,
			final boolean complete, final String toString) {
		this.lhs = lhs;
		this.index = index;
		this.symbolic = symbolic;
		this.literals = literals;
		this.predict = predict;
		this.rightCycle = rightCycle;
		this.toString = toString;
		this.complete = complete;
	}

	@Override
	public int compareTo(final SymbolState o) {
		if (lhs.equals(o.lhs)) {
			return index - o.index;
		}
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean equals(Object obj) {
		try{
			SymbolState o = (SymbolState)obj;
			return lhs.equals(o.lhs) && index == o.index; 
		}catch(Exception e){
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(lhs, index);
	}

	public Set<Symbol> pending() {
		return symbolic.keySet();
	}

	public Symbol lhs() {
		return lhs;
	}

	public String name() {
		return lhs.name() + "." + index;
	}

	public SymbolState accept(final int codepoint) {
		return literals.apply(codepoint);
	}

	public SymbolState accept(final Symbol trans) {
		return symbolic.get(trans);
	}

	public Set<Symbol> predict() {
		return predict;
	}

	public boolean isComplete() {
		return complete;
	}

	public Symbol rightCycle() {
		return rightCycle;
	}

	@Override
	public String toString() {
		return toString;
	}
}