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

import javax.inject.Inject;
import javax.inject.Named;

import org.echovantage.gild.Gild;
import org.echovantage.gild.proxy.FileSystemProxy;
import org.echovantage.gild.transform.Transformer;
import org.echovantage.inject.Variable;
import org.echovantage.util.Charsets;
import org.junit.Rule;
import org.junit.Test;

public class TransformedGildTest {
	private class TestModule {
		@Named("files.path")
		String path = "target/files";

		@Variable(Named.class)
		Path path(final @Variable(Named.class) String path) {
			return Paths.get(path);
		}

		@Named("files.transform")
		Transformer transform = recurse(with(path -> "output.txt".equals(path.getFileName()), line(UTF8, line -> line.replaceAll("\\d", "0")), sort(UTF8)));
	}

	@Rule
	public final Gild gild = new Gild(TestModule.class);
	@Inject
	@Named("files")
	private FileSystemProxy files;
	@Inject
	@Named("files.path")
	private Path working;
	private static final Charset UTF8 = Charsets.UTF_8;
	private final List<String> testStrings = Arrays.asList("alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel", "india");

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
