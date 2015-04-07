/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.echovantage.test;

import static java.nio.charset.Charset.forName;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static org.echovantage.util.Assert2.assertThrown;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Named;

import org.echovantage.gild.Gild;
import org.echovantage.gild.proxy.FileSystemProxy;
import org.echovantage.inject.Variable;
import org.junit.Rule;
import org.junit.Test;

public class FileSystemTest {
	private class TestModule {
		@Named("files.path")
		String path = "target/files";

		@Variable(Named.class)
		Path path(final @Variable(Named.class) String path) {
			return Paths.get(path);
		}
	}

	@Rule
	public final Gild gild = new Gild(TestModule.class);
	@Inject
	@Named("files")
	private FileSystemProxy files;
	@Inject
	@Named("files.path")
	private Path working;

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
