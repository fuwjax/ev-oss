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
package org.echovantage.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class ReadOnlyPath implements Comparable<ReadOnlyPath> {
	public static ReadOnlyPath readOnly(final Path path) {
		return path == null ? null : new ReadOnlyPath(path);
	}

	private final Path path;

	private ReadOnlyPath(final Path path) {
		this.path = path;
	}

	public InputStream newInputStream() throws IOException {
		return Files.newInputStream(path);
	}

	public boolean exists() {
		return Files.exists(path);
	}

	public boolean isDirectory() {
		return Files.isDirectory(path);
	}

	public DirectoryStream<ReadOnlyPath> newDirectoryStream() throws IOException {
		return new DirectoryStream<ReadOnlyPath>() {
			private final DirectoryStream<Path> stream = Files.newDirectoryStream(path);

			@Override
			public void close() throws IOException {
				stream.close();
			}

			@Override
			public Iterator<ReadOnlyPath> iterator() {
				return new Iterator<ReadOnlyPath>() {
					private final Iterator<Path> iter = stream.iterator();

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public ReadOnlyPath next() {
						return new ReadOnlyPath(iter.next());
					}
				};
			}
		};
	}

	public String getFileName() {
		return path.getFileName().toString();
	}

	public ReadOnlyPath resolve(final String relative) {
		return new ReadOnlyPath(path.resolve(relative));
	}

	public void copyTo(final Path dest) throws IOException {
		Files2.copy(path, dest);
	}

	public ReadOnlyPath getParent() {
		return new ReadOnlyPath(path.getParent());
	}

	@Override
	public String toString() {
		return getFileName();
	}

	@Override
	public int compareTo(final ReadOnlyPath o) {
		return path.compareTo(o.path);
	}

	public void copyTo(final OutputStream out) throws IOException {
		Files.copy(path, out);
	}
}
