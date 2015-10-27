package org.fuwjax.parser.bnf;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.fuwjax.parser.bnf.BnfGrammar.Builder;

public class Rule {
	private final String lhs;
	private final List<Expression> expression;
	private final boolean useIgnore;

	public Rule(final String lhs, final List<Expression> expression, final boolean useIgnore) {
		this.lhs = lhs;
		this.expression = expression == null ? Collections.emptyList() : expression;
		this.useIgnore = useIgnore;
	}

	public void applyTo(final Builder builder) {
		builder.rule(useIgnore, lhs, Function.identity(), expression.stream().map(x -> x.toStep(builder)).toArray());
	}

	@Override
	public String toString() {
		return "Rule[" + lhs + (useIgnore ? ":= " : "= ") + expression + "]";
	}
}
