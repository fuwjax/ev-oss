package org.echovantage.test;

import static java.nio.charset.Charset.forName;
import static org.echovantage.gild.transform.StreamTransform.line;
import static org.echovantage.gild.transform.StreamTransform.sort;
import static org.echovantage.gild.transform.Transformer.recurse;
import static org.echovantage.gild.transform.Transformer.with;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.echovantage.gild.Gild;
import org.echovantage.gild.proxy.FileSystemProxy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TransformedGildTest {
	private static final Charset UTF8 = forName("UTF-8");
	private final List<String> testStrings = Arrays.asList("alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel", "india");
	private final FileSystemProxy files = new FileSystemProxy();
	@Rule
	public final Gild gild = new Gild().with("files", files).transformedBy(recurse(with(path -> "output.txt".equals(path.getFileName()), line(UTF8, line -> line.replaceAll("\\d", "0")), sort(UTF8))));
	private Path working;

	@Before
	public void setup() {
		working = Paths.get("target/files");
		files.setWorkingDirectory(working);
	}

	@Test
	public void testNonDeterministic() throws IOException {
		Collections.shuffle(testStrings);
		try(BufferedWriter writer = Files.newBufferedWriter(working.resolve("output.txt"))) {
			for(final String s : testStrings) {
				writer.append(Long.toString(System.currentTimeMillis())).append(",").append(s).append("\n");
			}
		}
	}
}
