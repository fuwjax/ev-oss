package org.fuwjax.parser.bnf;

import org.fuwjax.parser.bnf.BnfGrammar.Builder;
import org.fuwjax.parser.builder.Codepoints;

public class CharClass implements Expression{
	private Codepoints codepoints;

	public CharClass(Codepoints codepoints) {
		this.codepoints = codepoints;
	}

	@Override
	public Object toStep(Builder builder) {
		return codepoints;
	}
}
