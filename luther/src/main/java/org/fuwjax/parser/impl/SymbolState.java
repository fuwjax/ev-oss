package org.fuwjax.parser.impl;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;

public class SymbolState {
    private Symbol lhs;
    private String toString;
    private Map<Symbol, SymbolState> symbolic;
    private IntFunction<SymbolState> literals;
    private Set<Symbol> predict;
    private Symbol rightCycle;
	private Function<? super Model, ? extends Node> transform;
	private int index;

    public void init(Symbol lhs, int index, Map<Symbol, SymbolState> symbolic, IntFunction<SymbolState> literals, Set<Symbol> predict, Symbol rightCycle, Function<? super Model, ? extends Node> transform, String toString) {
        this.lhs = lhs;
		this.index = index;
        this.symbolic = symbolic;
        this.literals = literals;
        this.predict = predict;
        this.rightCycle = rightCycle;
        this.toString = toString;
		this.transform = transform;
    }

    public Set<Symbol> pending() {
        return symbolic.keySet();
    }

    public Symbol lhs() {
        return lhs;
    }
    
    public String name(){
    	return lhs.name()+"."+index;
    }

    public SymbolState accept(int codepoint) {
        return literals.apply(codepoint);
    }

    public SymbolState accept(Symbol trans) {
        return symbolic.get(trans);
    }

    public Set<Symbol> predict() {
        return predict;
    }

    public boolean isComplete() {
        return transform != null;
    }

    public Symbol rightCycle() {
        return rightCycle;
    }

    @Override
    public String toString() {
        return toString;
    }
    
	public Node result(Model model) {
		return transform.apply(model);
	}
}