package org.fuwjax.parser.bnf;

import java.util.List;
import java.util.stream.Collectors;

import org.fuwjax.parser.bnf.BnfGrammar.Builder;

public class Literal {
	private String literal;

	public Literal(String literal){
		this.literal = literal;
	}

	public List<Expression> toExpressions() {
		return literal.codePoints().mapToObj(cp -> Builder.of(cp)).map(CharClass::new).collect(Collectors.toList());
	}
}
