package org.fuwjax.oss.test;

import static org.fuwjax.oss.util.assertion.Assert2.assertThrown;
import static org.fuwjax.oss.util.io.IntReader.codepoints;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.fuwjax.oss.sample.SimpleGrammarBuilder;
import org.fuwjax.parser.Grammar;
import org.fuwjax.parser.Model;
import org.fuwjax.parser.Node;
import org.fuwjax.parser.impl.Char;
import org.fuwjax.parser.impl.StandardModel;
import org.junit.Before;
import org.junit.Test;

public class GrammarTest {
	private SimpleGrammarBuilder g;
	private Grammar grammar;

	@Before
	public void setup() {
		g = new SimpleGrammarBuilder();
	}

	@Test
	public void testSimple() throws Exception {
		g.rule("S", "a");
		grammar = g.build("S");
		assertEquals("a", parse("a").match());
		assertThrown(IllegalArgumentException.class, () -> parse("b"));
	}

	@Test
	public void testSymbol() throws Exception {
		g.rule("S", "a");
		g.rule("C", "b");
		g.rule("S", "C");
		grammar = g.build("S");
		assertEquals("a", parse("a").match());
		assertEquals("b", parse("b").match());
		assertThrown(IllegalArgumentException.class, () -> parse("c"));
	}

	private Model n(final String id, final Node... children) {
		return new StandardModel(g.symbol(id).build(), children);
	}

	private Node t(final int ch) {
		return new Char(ch);
	}

	@Test
	public void testAmbiguous() throws Exception {
		g.rule("S", "sS");
		g.rule("S", "IS");
		g.rule("S", "I");
		g.rule("S", "s");
		g.rule("I", "iSE");
		g.rule("I", "iS");
		g.rule("E", "eS");
		grammar = g.build("S");
		assertEquals(n("S", t('s')), parse("s"));
		assertEquals(n("S", n("I", t('i'), n("S", t('s')))), parse("is"));
		assertEquals(n("S", n("I", t('i'), n("S", t('s')), n("E", t('e'), n("S", t('s'))))), parse("ises"));
		assertEquals(n("S", n("I", t('i'), n("S", n("I", t('i'), n("S", t('s')))))), parse("iis"));
		assertEquals(n("S", n("I", t('i'), n("S", n("I", t('i'), n("S", t('s')), n("E", t('e'), n("S", t('s'))))))),
				parse("iises"));
	}

	@Test
	public void testRightRecurse() throws Exception {
		g.rule("S", "aS");
		g.rule("C", "");
		g.rule("S", "C");
		g.rule("C", "aCb");
		grammar = g.build("S");
		assertEquals(n("S", t('a'), n("S", t('a'), n("S", t('a'), n("S", t('a'))))), parse("aaaa"));
		assertEquals(n("S", t('a')), parse("a"));
		assertEquals(n("S", n("C", t('a'), t('b'))), parse("ab"));
		assertEquals(n("S", n("C", t('a'), n("C", t('a'), t('b')), t('b'))), parse("aabb"));
		assertEquals(n("S", t('a'), n("S", n("C", t('a'), n("C", t('a'), t('b')), t('b')))), parse("aaabb"));
		assertNull(parse(""));
		assertThrown(IllegalArgumentException.class, () -> parse("abb"));
	}

	@Test
	public void testSomethingNatural() throws Exception {
		g.rule("S", "SL");
		g.rule("S", "L");
		g.rule("L", "Xn");
		g.rule("X", "Xx");
		g.rule("X", "x");
		grammar = g.build("S");
		assertEquals(
				n("S", n("S", n("S", n("L", n("X", n("X", n("X", t('x')), t('x')), t('x')), t('n'))),
						n("L", n("X", t('x')), t('n'))), n("L", n("X", n("X", t('x')), t('x')), t('n'))),
				parse("xxxnxnxxn"));
		assertEquals(n("S", n("L", n("X", t('x')), t('n'))), parse("xn"));
		assertEquals(n("S", n("S", n("S", n("S", n("L", n("X", t('x')), t('n'))), n("L", n("X", t('x')), t('n'))),
				n("L", n("X", t('x')), t('n'))), n("L", n("X", t('x')), t('n'))), parse("xnxnxnxn"));
		assertThrown(IllegalArgumentException.class, () -> parse(""));
		assertThrown(IllegalArgumentException.class, () -> parse("xxnxx"));
		assertThrown(IllegalArgumentException.class, () -> parse("nxxxn"));
		assertThrown(IllegalArgumentException.class, () -> parse("xxnnxxn"));
	}

	private Model parse(final String input) throws IOException {
		return (Model) grammar.parse(codepoints(input));
	}
}
