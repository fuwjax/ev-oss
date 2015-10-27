package org.fuwjax.parser.bnf;

import static java.util.function.Function.identity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.fuwjax.oss.util.io.IntReader;
import org.fuwjax.parser.Grammar;
import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;
import org.fuwjax.parser.builder.Codepoints;
import org.fuwjax.parser.builder.GrammarBuilder;
import org.fuwjax.parser.builder.SymbolBuilder;
import org.fuwjax.parser.builder.SymbolStateBuilder;

public class BnfGrammar {
	static class Builder extends GrammarBuilder {
		private Builder standardBnfRules() {
			rule(false, "#ignore", identity(), any(' ', '\n', '\t', '\r'));
			rule(true, "#start", this::grammar, symbol("rules"));
			rule(true, "rules", this::rules, symbol("directive"), symbol("rules"));
			rule(true, "rules", this::rules, symbol("rule"), symbol("rules"));
			rule(true, "rules", this::rules, symbol("pattern"), symbol("rules"));
			rule(true, "rules", this::rules);
			rule(true, "directive", this::directive, of('#'), symbol("symbol"), symbol("expression"));
			rule(true, "pattern", this::pattern, symbol("symbol"), of('='), symbol("expression"));
			rule(true, "rule", this::rule, symbol("symbol"), of(':'), of('='), symbol("expression"));
			rule(true, "expression", this::expression, symbol("symbol"), symbol("expression"));
			rule(true, "expression", this::expression, symbol("literal"), symbol("expression"));
			rule(true, "expression", this::expression, symbol("class"), symbol("expression"));
			rule(true, "expression", this::expression);

			rule(false, "symbol", this::reference, of('_').range('A', 'Z').range('a', 'z'), symbol("symboltail"));
			rule(false, "symboltail", identity(), of('_').range('A', 'Z').range('a', 'z').range('0', '9'),
					symbol("symboltail"));
			rule(false, "symboltail", identity());
			rule(false, "literal", this::literal, of('\''), symbol("single"), of('\''));
			rule(false, "single", identity(), any('\'', '\\').negate(), symbol("single"));
			rule(false, "single", identity(), symbol("escape"), symbol("single"));
			rule(false, "single", identity());
			rule(false, "escape", this::escape, of('\\'), any().negate());
			rule(false, "class", this::charClass, of('['), symbol("chars"), of(']'));
			rule(false, "class", this::charClass, of('['), of('^'), symbol("chars"), of(']'));
			rule(false, "chars", this::chars, symbol("char"), symbol("chars"));
			rule(false, "chars", this::chars, symbol("range"), symbol("chars"));
			rule(false, "chars", this::chars);
			rule(false, "char", identity(), any('\\', ']', '-').negate());
			rule(false, "char", identity(), symbol("escape"));
			rule(false, "range", identity(), symbol("char"), of('-'), symbol("char"));
			return this;
		}

		private Grammar grammar(final Model model) {
			final List<Rule> rules = (List<Rule>) model.getValue("rules");
			final Builder builder = new Builder();
			rules.forEach(rule -> rule.applyTo(builder));
			return builder.build("#start");
		}

		private List<Rule> rules(final Model model) {
			final Rule rule = (Rule) model.getValue("directive", "rule");
			List<Rule> rules = (List<Rule>) model.getValue("rules");
			if (rules == null) {
				rules = new ArrayList<>();
			}
			if (rule != null) {
				rules.add(rule);
			}
			return rules;
		}

		private Rule directive(final Model model) {
			return new Rule("#" + ((Reference) model.getValue("symbol")).name(),
					(List<Expression>) model.getValue("expression"), true);
		}

		private Rule pattern(final Model model) {
			return new Rule(((Reference) model.getValue("symbol")).name(),
					(List<Expression>) model.getValue("expression"), false);
		}

		private Rule rule(final Model model) {
			return new Rule(((Reference) model.getValue("symbol")).name(),
					(List<Expression>) model.getValue("expression"), true);
		}

		private List<Expression> expression(final Model model) {
			List<Expression> expressions = (List<Expression>) model.getValue("rules");
			if (expressions == null) {
				expressions = new ArrayList<>();
			}
			final Expression expression = (Expression) model.getValue("symbol", "class");
			if (expression != null) {
				expressions.add(expression);
			} else {
				final Literal literal = (Literal) model.getValue("literal");
				if (literal != null) {
					expressions.addAll(literal.toExpressions());
				}
			}
			return expressions;
		}

		private Reference reference(final Model model) {
			return new Reference(model.match());
		}

		private Literal literal(final Model model) {
			final Model single = model.get("single");
			return single == null ? new Literal("") : new Literal(single.match());
		}

		private Node escape(final Model model) {
			final Node ch = model.node(1);
			switch ((int) ch.value()) {
			case 'n':
				return Node.codepoint('\n');
			case 'r':
				return Node.codepoint('\r');
			case 't':
				return Node.codepoint('\t');
			default:
				return ch;
			}
		}

		private Object charClass(final Model model) {
			Codepoints chars = (Codepoints) model.getValue("chars");
			if (chars == null) {
				chars = new Codepoints();
			}
			if (Objects.equals(model.node(1).value(), '^')) {
				chars.negate();
			}
			return new CharClass(chars);
		}

		private Object chars(final Model model) {
			Codepoints chars = (Codepoints) model.getValue("chars");
			if (chars == null) {
				chars = new Codepoints();
			}
			final Model roc = model.get("range", "char");
			if (roc != null) {
				final String match = roc.match();
				if ("range".equals(roc.symbol().name())) {
					chars.range(match.codePointAt(0), match.codePointAt(2));
				} else {
					chars.add(match.codePointAt(0));
				}
			}
			return chars;
		}

		SymbolBuilder rule(final boolean useIgnore, final String lhs, final Function<Model, ?> transform,
				final Object... steps) {
			final SymbolBuilder s = symbol(lhs, Model.wrap(transform));
			SymbolStateBuilder state = s.start();
			final String[] names = names(steps);
			int index = 0;
			for (final Object step : steps) {
				if (useIgnore && index > 0) {
					state = state.ensure(name(index, names), symbol("#ignore"));
				}
				if (step instanceof SymbolBuilder) {
					state = state.ensure(name(index, names), (SymbolBuilder) step);
				} else if (step instanceof Codepoints) {
					state = state.ensure(name(index, names), (Codepoints) step);
				}
				++index;
			}
			if ("#start".equals(lhs) || "#ignore".equals(lhs)) {
				s.start().complete(name(0, names));
				state = state.ensure(name(index, names), symbol("#ignore"));
			}
			state.complete(name(index, names));
			return s;
		}

		private static String[] names(final Object... steps) {
			final String[] names = new String[steps.length];
			for (int i = 0; i < names.length; i++) {
				if (steps[i] instanceof SymbolBuilder) {
					names[i] = ((SymbolBuilder) steps[i]).name();
				} else if (steps[i] instanceof Codepoints) {
					names[i] = steps[i].toString();
				}
			}
			return names;
		}

		private static String name(final int dot, final String... names) {
			final StringBuilder builder = new StringBuilder();
			for (int i = 0; i < names.length; i++) {
				builder.append(i == dot ? '.' : i > 0 ? ' ' : "").append(names[i]);
			}
			if (dot >= names.length) {
				builder.append('.');
			}
			return builder.toString();
		}

		static Codepoints of(final int option) {
			return new Codepoints().add(option);
		}

		private static Codepoints any(final int... options) {
			return new Codepoints().add(options);
		}
	}

	private final Grammar bnf = new Builder().standardBnfRules().build("#start");

	public Grammar grammar(final IntReader input) throws IOException {
		return (Grammar) bnf.parse(input);
	}

	@Override
	public String toString() {
		return bnf.toString();
	}
}
