package org.fuwjax.oss.test;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static org.fuwjax.oss.util.io.IntReader.utf8ToCodepoint;

import java.io.IOException;
import java.io.InputStream;

import org.fuwjax.oss.gild.Gild;
import org.fuwjax.oss.gild.proxy.FileSystemProxy;
import org.fuwjax.parser.Grammar;
import org.fuwjax.parser.bnf.BnfGrammar;
import org.junit.Rule;
import org.junit.Test;

public class BnfTest {
	private final BnfGrammar BNF = new BnfGrammar();
	@Rule
	public final Gild gild = new Gild().with("file", new FileSystemProxy(get("target/bnf")));

	@Test
	public void testBnf() throws IOException {
		System.out.println(BNF);
		try (InputStream bnfFile = newInputStream(get("target/bnf/sample.bnf"))) {
			final Grammar sample = BNF.grammar(utf8ToCodepoint(bnfFile));
			try (InputStream testFile = newInputStream(get("target/bnf/sample.csv"))) {
				System.out.println(sample.parse(utf8ToCodepoint(testFile)));
			}
		}
	}
}
