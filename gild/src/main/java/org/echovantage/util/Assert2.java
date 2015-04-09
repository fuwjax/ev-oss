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
package org.echovantage.util;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walkFileTree;
import static org.echovantage.util.Files2.relativize;
import static org.echovantage.util.Files2.resolve;
import static org.echovantage.util.assertion.Assertions.assertThat;
import static org.echovantage.util.assertion.Assertions.asserts;
import static org.echovantage.util.assertion.Assertions.fails;
import static org.echovantage.util.assertion.Assertions.failsToReturn;
import static org.echovantage.util.assertion.Assertions.failsToReturnWith;
import static org.echovantage.util.assertion.Assertions.failsWith;
import static org.echovantage.util.assertion.Assertions.isException;
import static org.echovantage.util.assertion.Assertions.isJustA;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.echovantage.util.assertion.Assertion;
import org.echovantage.util.assertion.Assertions;
import org.echovantage.util.function.UnsafeRunnable;

public class Assert2 {
	public static void assertCompletes(final UnsafeRunnable whenCalled) {
		try {
			whenCalled.run();
		} catch(final AssertionError e) {
			throw e;
		} catch(final Throwable t) {
			throw new AssertionError("Runnable did not complete", t);
		}
	}

	public static <T> T assertReturns(final Callable<T> whenCalled) {
		try {
			return whenCalled.call();
		} catch(final AssertionError e) {
			throw e;
		} catch(final Throwable t) {
			throw new AssertionError("Callable did not compete", t);
		}
	}

	public static void assertFails(final UnsafeRunnable whenCalled) {
		assertThat(whenCalled, fails());
	}

	public static void assertFails(final Callable<?> whenCalled) {
		assertThat(whenCalled, failsToReturn());
	}

	public static void assertThrown(final Class<? extends Throwable> expected, final UnsafeRunnable whenCalled) {
		assertThat(whenCalled, failsWith(isJustA(expected)));
	}

	public static void assertThrown(final Throwable expected, final UnsafeRunnable whenCalled) {
		assertThat(whenCalled, failsWith(isException(expected)));
	}

	public static void assertThrown(final Class<? extends Throwable> expected, final Callable<?> whenCalled) {
		assertThat(whenCalled, failsToReturnWith(isJustA(expected)));
	}

	public static void assertThrown(final Throwable expected, final Callable<?> whenCalled) {
		assertThat(whenCalled, failsToReturnWith(isException(expected)));
	}

	public static void assertEquals(final Throwable expected, final Throwable actual) {
		assertThat(actual, isException(expected));
	}

	public static Assertion<Path> existsIn(final Path srcRoot, final Path testRoot) {
		return Assertions.asserts(() -> "exists in " + srcRoot + " and therefore in " + testRoot, p -> {
			final Path sub = relativize(srcRoot, p);
			return Files.exists(resolve(testRoot, sub));
		});
	};

	/**
	 * Asserts that every file that exists relative to expected also exists
	 * relative to actual.
	 * @param expected the expected path
	 * @param actual the actual path
	 * @throws IOException if the paths cannot be walked
	 */
	public static void containsAll(final Path expected, final Path actual) throws IOException {
		final Assertion<Path> exists = existsIn(expected, actual);
		if(Files.exists(expected)) {
			walkFileTree(expected, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					assertThat(file, exists);
					return super.visitFile(file, attrs);
				}
			});
		}
	}

	/**
	 * Asserts that two paths are deeply byte-equivalent.
	 * @param expected one of the paths
	 * @param actual the other path
	 * @throws IOException if the paths cannot be traversed
	 */
	public static void assertEquals(final Path expected, final Path actual) throws IOException {
		containsAll(actual, expected);
		if(Files.exists(expected)) {
			containsAll(expected, actual);
			walkFileTree(expected, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					final Path sub = relativize(expected, file);
					final Path therePath = resolve(actual, sub);
					final long hereSize = Files.size(file);
					final long thereSize = Files.size(therePath);
					assertThat(thereSize, asserts(() -> sub + " is " + hereSize + " bytes", t -> t == hereSize));
					assertByteEquals(sub, file, therePath);
					return super.visitFile(file, attrs);
				}
			});
		}
	}

	/**
	 * Asserts that two paths are byte-equivalent.
	 * @param sub the shared portion of the two paths
	 * @param expected the expected path
	 * @param actual the actual path
	 * @throws IOException if the paths cannot be opened and consumed
	 */
	public static void assertByteEquals(final Path sub, final Path expected, final Path actual) throws IOException {
		final int length = 4096;
		final byte[] hereBuffer = new byte[length];
		final byte[] thereBuffer = new byte[length];
		long hereLimit = 0;
		long thereLimit = 0;
		try(InputStream hereStream = newInputStream(expected); InputStream thereStream = newInputStream(actual)) {
			int line = 1;
			int ch = 0;
			for(long i = 0; i < Files.size(expected); i++) {
				if(i >= hereLimit) {
					hereLimit += read(hereStream, hereBuffer, hereLimit);
				}
				if(i >= thereLimit) {
					thereLimit += read(thereStream, thereBuffer, thereLimit);
				}
				final int c = hereBuffer[(int) (i % length)];
				assertThat(thereBuffer[(int) (i % length)], asserts(message(sub, i, line, ch), t -> t == c));
				if(c == '\n') {
					ch = 0;
					line++;
				} else {
					ch++;
				}
			}
		}
	}

	private static Supplier<String> message(final Path sub, final long i, final int line, final int ch) {
		return () -> sub + " does not match at byte " + i + " line " + line + " column " + ch;
	}

	private static int read(final InputStream stream, final byte[] buffer, final long limit) throws IOException {
		final int offset = (int) (limit % buffer.length);
		final int count = stream.read(buffer, offset, buffer.length - offset);
		if(count == -1) {
			throw new EOFException();
		}
		return count;
	}
}
