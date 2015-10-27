package org.fuwjax.parser.bnf;

import org.fuwjax.parser.bnf.BnfGrammar.Builder;
import org.fuwjax.parser.builder.Codepoints;

public class CharClass implements Expression {
	private final Codepoints codepoints;

	public CharClass(final Codepoints codepoints) {
		this.codepoints = codepoints;
	}

	@Override
	public Object toStep(final Builder builder) {
		return codepoints;
	}

	@Override
	public String toString() {
		return "Class" + codepoints;
	}
}
