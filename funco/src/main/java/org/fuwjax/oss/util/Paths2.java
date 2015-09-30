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
package org.fuwjax.oss.util;

import static java.lang.ClassLoader.getSystemResource;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Paths2 {
	private static FileSystem getFileSystem(final URI uri) throws IOException {
		try {
			return FileSystems.getFileSystem(uri);
		} catch(final FileSystemNotFoundException e) {
			return FileSystems.newFileSystem(uri, emptyMap());
		}
	}

	public static Path classpath(final String... paths) throws IOException, URISyntaxException {
		try {
			final URI resource = getSystemResource(asList(paths).stream().collect(joining("/"))).toURI();
			final String[] array = resource.toString().split("!");
			if(array.length == 2) {
			final FileSystem fs = getFileSystem(URI.create(array[0]));
			return fs.getPath(array[1]);
			}
			return Paths.get(resource);
		} catch(final NullPointerException e) {
			throw new RuntimeException("Could not find " + asList(paths).stream().collect(joining("/")), e);
		}
	}
}
