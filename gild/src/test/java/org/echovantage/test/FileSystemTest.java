package org.echovantage.test;

import org.echovantage.gild.Gild;
import org.echovantage.gild.proxy.FileSystemProxy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.Charset.forName;
import static java.nio.file.Files.*;
import static java.util.Arrays.asList;
import static org.echovantage.util.Assert2.assertThrown;
import static org.junit.Assert.*;

public class FileSystemTest {
	private final FileSystemProxy files = new FileSystemProxy();
	@Rule
	public final Gild gild = new Gild().with("files", files);
	private Path working;

	@Before
	public void setup() {
		working = Paths.get("target/files");
		files.setWorkingDirectory(working);
	}

	@Test
	public void testFiles() throws IOException {
		try(BufferedReader reader = newBufferedReader(working.resolve("query.txt"), forName("UTF-8"))) {
			assertEquals("Are you thinking what I'm thinking?", reader.readLine());
		}
		write(working.resolve("response.txt"), asList("I think so, but burlap chafes me so."));
	}

	@Test
	public void testDelete() throws IOException {
		deleteIfExists(working.resolve("query.txt"));
	}

	@Test
	public void testNoInput() throws IOException {
		write(working.resolve("response.txt"), asList("I think so, but burlap chafes me so."));
	}

	@Test
	public void testMissingOutput() throws IOException {
		write(working.resolve("response.txt"), asList("I think so, but burlap chafes me so."));
		assertThrown(AssertionError.class, gild::assertGolden);
	}
}
