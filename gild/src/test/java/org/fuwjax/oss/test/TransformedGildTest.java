/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
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
package org.fuwjax.oss.test;

import static java.nio.charset.Charset.forName;
import static org.fuwjax.oss.gild.transform.StreamTransform.line;
import static org.fuwjax.oss.gild.transform.StreamTransform.sort;
import static org.fuwjax.oss.gild.transform.Transformer.recurse;
import static org.fuwjax.oss.gild.transform.Transformer.with;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fuwjax.oss.gild.Gild;
import org.fuwjax.oss.gild.proxy.FileSystemProxy;
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
