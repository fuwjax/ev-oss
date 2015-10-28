package org.fuwjax.parser.bnf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
			transform("#start", this::grammar);
			transform("rules", this::rules);
			transform("directive", this::directive);
			transform("pattern", this::pattern);
			transform("rule", this::rule);
			transform("expression", this::expression);
			transform("symbol", this::reference);
			transform("literal", this::literal);
			transform("escape", this::escape);
			transform("class", this::charClass);
			transform("chars", this::chars);
			
			rule(false, "#ignore", symbol("WS"));
			rule(true, "#start", symbol("rules"));
			rule(true, "rules", symbol("directive"), symbol("rules"));
			rule(true, "rules", symbol("rule"), symbol("rules"));
			rule(true, "rules", symbol("pattern"), symbol("rules"));
			rule(true, "rules");
			rule(true, "directive", of('#'), symbol("symbol"), symbol("expression"));
			rule(true, "pattern", symbol("symbol"), of('='), symbol("expression"));
			rule(true, "rule",symbol("symbol"), of(':'), of('='), symbol("expression"));
			rule(true, "expression", symbol("symbol"), symbol("expression"));
			rule(true, "expression", symbol("literal"), symbol("expression"));
			rule(true, "expression", symbol("class"), symbol("expression"));
			rule(true, "expression");

			rule(false, "symbol", of('_').range('A', 'Z').range('a', 'z'), symbol("symboltail"));
			rule(false, "symboltail", of('_').range('A', 'Z').range('a', 'z').range('0', '9'),
					symbol("symboltail"));
			rule(false, "symboltail");
			rule(false, "literal", of('\''), symbol("single"), of('\''));
			rule(false, "single", any('\'', '\\').negate(), symbol("single"));
			rule(false, "single", symbol("escape"), symbol("single"));
			rule(false, "single");
			rule(false, "escape", of('\\'), any().negate());
			rule(false, "class", of('['), symbol("chars"), of(']'));
			rule(false, "class", of('['), of('^'), symbol("chars"), of(']'));
			rule(false, "chars", symbol("char"), symbol("chars"));
			rule(false, "chars", symbol("range"), symbol("chars"));
			rule(false, "chars");
			rule(false, "char", any('\\', ']', '-').negate());
			rule(false, "char", symbol("escape"));
			rule(false, "range", symbol("char"), of('-'), symbol("char"));
			rule(false, "WS", any(' ', '\n', '\t', '\r'));
			rule(false, "WS", any(' ', '\n', '\t', '\r'), symbol("WS"));
			return this;
		}
		
		private Grammar grammar(final Model model) {
			final List<Rule> rules = (List<Rule>) model.getValue("rules");
			if(rules == null){
				return null;
			}
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
			List<Expression> expressions = (List<Expression>) model.getValue("expression");
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

		SymbolBuilder rule(final boolean useIgnore, final String lhs, 
				final Object... steps) {
			final SymbolBuilder s = symbol(lhs);
			SymbolStateBuilder state = s.start();
			final String[] names = names(steps);
			int index = 0;
			boolean previousSymbol = false;
			for (final Object step : steps) {
				if (step instanceof SymbolBuilder) {
					if (useIgnore && index > 0) {
						state = state.ensure(name(index, names), symbol(previousSymbol ? "#ignore0" : "#ignore"));
					}
					state = state.ensure(name(index, names), (SymbolBuilder) step);
					previousSymbol = true;
				} else if (step instanceof Codepoints) {
					if (useIgnore && index > 0) {
						state = state.ensure(name(index, names), symbol("#ignore"));
					}
					state = state.ensure(name(index, names), (Codepoints) step);
					previousSymbol = false;
				}
				++index;
			}
			if ("#start".equals(lhs)) {
				s.start().complete(name(0, names));
				state = state.ensure(name(index, names), symbol("#ignore"));
			}
			if("#ignore".equals(lhs)){
				s.start().complete(name(0, names));
				SymbolBuilder ignored = symbol("#ignore0");
				ignored.start().ensure(name(0, names), (SymbolBuilder)steps[0]).complete(name(1, names));;
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
