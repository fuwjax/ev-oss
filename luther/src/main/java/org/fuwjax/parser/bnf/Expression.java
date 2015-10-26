package org.fuwjax.parser.bnf;

import org.fuwjax.parser.bnf.BnfGrammar.Builder;

public interface Expression {
	Object toStep(Builder builder);
}
