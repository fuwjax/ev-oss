package org.echovantage.test;

import static java.nio.charset.Charset.forName;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static org.echovantage.gild.stage.StandardStageFactory.startingAt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.echovantage.gild.Gild;
import org.echovantage.gild.proxy.FileSystemProxy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class StagedGildTest {
	private final FileSystemProxy files = new FileSystemProxy();
	@Rule
	public final Gild gild = new Gild().with("files", files).staged(startingAt("stage1"));
	private Path working;

	@Before
	public void setup() {
		working = Paths.get("target/files");
		files.setWorkingDirectory(working);
	}

	@Test
	public void testStages() throws IOException {
		try(BufferedReader reader = newBufferedReader(working.resolve("query.txt"), forName("UTF-8"))) {
			assertEquals("Are you thinking what I'm thinking?", reader.readLine());
		}
		write(working.resolve("response.txt"), asList("I think so, but burlap chafes me so."));
		assertFalse(Files.exists(working.resolve("other.txt")));
		gild.nextStage("stage2");
		try(BufferedReader reader = newBufferedReader(working.resolve("other.txt"), forName("UTF-8"))) {
			assertEquals("But me and Pippi Longstocking, what will the children be like?", reader.readLine());
		}
	}
}
