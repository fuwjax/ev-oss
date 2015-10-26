package org.fuwjax.parser.bnf;

import org.fuwjax.parser.bnf.BnfGrammar.Builder;

public class Reference implements Expression{
	private String name;

	public Reference(String name){
		this.name = name;
	}
	
	public String name(){
		return name;
	}
	
	@Override
	public Object toStep(Builder builder) {
		return builder.symbol(name);
	}
}
