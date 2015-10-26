package org.fuwjax.parser.bnf;

import static java.util.function.Function.identity;
import static org.fuwjax.parser.Model.named;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fuwjax.oss.util.io.IntReader;
import org.fuwjax.parser.Grammar;
import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;
import org.fuwjax.parser.builder.Codepoints;
import org.fuwjax.parser.builder.GrammarBuilder;
import org.fuwjax.parser.builder.SymbolBuilder;
import org.fuwjax.parser.builder.SymbolStateBuilder;
import org.fuwjax.parser.impl.MigratedModel;
import org.fuwjax.parser.impl.Value;

public class BnfGrammar {
	static class Builder extends GrammarBuilder {
		private Builder standardBnfRules() {
			rule(false, "#ignore", Model::match, any(' ', '\n', '\t', '\r'));
			rule(true, "#start", this::grammar, symbol("rules"));
			rule(true, "rules", this::add, symbol("directive"), symbol("rules"));
			rule(true, "rules", this::add, symbol("rule"), symbol("rules"));
			rule(true, "rules", this::newList);
			rule(true, "directive", this::rule, of('#'), symbol("symbol"), symbol("expression"));
			rule(true, "rule", this::rule, symbol("symbol"), of('='), symbol("expression"));
			rule(true, "rule", this::rule, symbol("symbol"), of(':'), of('='), symbol("expression"));
			rule(true, "expression", this::add, symbol("symbol"), symbol("expression"));
			rule(true, "expression", this::addAll, symbol("literal"), symbol("expression"));
			rule(true, "expression", this::add, symbol("class"), symbol("expression"));
			rule(true, "expression", this::newList);

			rule(false, "symbol", this::reference, of('_').range('A', 'Z').range('a', 'z'), symbol("symboltail"));
			rule(false, "symboltail", identity(), of('_').range('A', 'Z').range('a', 'z').range('0', '9'),
					symbol("symboltail"));
			rule(false, "symboltail", identity());
			rule(false, "literal", this::literal, of('\''), symbol("single"), of('\''));
			rule(false, "single", identity(), any('\'', '\\').negate(), symbol("single"));
			rule(false, "single", identity(), symbol("escape"), symbol("single"));
			rule(false, "single", identity());
			rule(false, "escape", m -> Node.codepoint('\n'), of('\\'), of('n'));
			rule(false, "escape", m -> Node.codepoint('\t'), of('\\'), of('t'));
			rule(false, "escape", m -> Node.codepoint('\r'), of('\\'), of('r'));
			rule(false, "escape", m -> node(m,1), of('\\'), any('n', 'r', 't').negate());
			rule(false, "class", this::charClass, of('['), symbol("chars"), of(']'));
			rule(false, "class", this::negateClass, of('['), of('^'), symbol("chars"), of(']'));
			rule(false, "chars", this::addChar, symbol("char"), symbol("chars"));
			rule(false, "chars", this::addRange, symbol("range"), symbol("chars"));
			rule(false, "chars", this::newClass);
			rule(false, "char", m -> node(m,0), any('\\', ']', '-').negate());
			rule(false, "char", this::pass, symbol("escape"));
			rule(false, "range", this::range, symbol("char"), of('-'), symbol("char"));
			return this;
		}

		private Grammar grammar(final Model model) {
			final List<Rule> rules = (List<Rule>) model.getValue("rules");
			final Builder builder = new Builder();
			rules.forEach(rule -> rule.applyTo(builder));
			return builder.build("#start");
		}

		private Reference reference(final Model model) {
			return new Reference(model.match());
		}

		private Literal literal(final Model model) {
			return new Literal(model.get("single").match());
		}
		
		private Node node(Model model, int index){
			return model.children().skip(index).findFirst().map(Node::result).orElse(null);
		}

		private List addAll(final Model model) {
			List expressions = (List) model.getValue("expression");
			if(expressions == null){
				expressions = new ArrayList();
			}
			expressions.addAll(((Literal) model.getValue("literal")).toExpressions());
			return expressions;
		}

		private Range range(final Model model) {
			final List<Model> chars = model.getAll("char").collect(Collectors.toList());
			return new Range((Integer) chars.get(0).value(), (Integer) chars.get(1).value());
		}

		private Stream<Model> children(final Model model) {
			return model.modelChildren().filter(named("#ignore").negate());
		}

		private Object negateClass(final Model model) {
			return new CharClass(((Codepoints) model.getValue("chars")).negate());
		}

		private Object charClass(final Model model) {
			return new CharClass((Codepoints) model.getValue("chars"));
		}

		private Object newClass(final Model model) {
			return new Codepoints();
		}

		private Object addChar(final Model model) {
			final Codepoints codepoints = (Codepoints) model.getValue("chars");
			codepoints.add((Integer) model.getValue("char"));
			return codepoints;
		}

		private class Range {
			private final int lo;
			private final int hi;

			public Range(final int lo, final int hi) {
				this.lo = lo;
				this.hi = hi;
			}
		}

		private Object addRange(final Model model) {
			final Codepoints codepoints = (Codepoints) model.getValue("chars");
			final Range range = (Range) model.getValue("range");
			codepoints.range(range.lo, range.hi);
			return codepoints;
		}

		private Object pass(final Model model) {
			return children(model).findAny().get().result();
		}

		private List add(final Model model) {
			final List<Model> children = children(model).collect(Collectors.toList());
			List list;
			if(children.size() == 1){
				list = new ArrayList();
			}else{
				list = (List) children.get(1).value();
			}
			final Object value = children.get(0).value();
			list.add(value);
			return list;
		}

		private List newList(final Model model) {
			return new ArrayList();
		}

		private Rule rule(final Model model) {
			return new Rule(((Reference) model.getValue("symbol")).name(), (List<Expression>) model.getValue("expression"));
		}
		
		SymbolBuilder rule(final boolean useIgnore, final String lhs, final Function<Model, ?> transform,
				final Object... steps) {
			final SymbolBuilder s = symbol(lhs);
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
				s.start().complete(name(0, names), Model.wrap(transform));
				state = state.ensure(name(index, names), symbol("#ignore"));
			}
			state.complete(name(index, names), Model.wrap(transform));
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
