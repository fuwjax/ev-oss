package org.echovantage.util;

import org.echovantage.util.assertion.Assertion;
import org.echovantage.util.assertion.Assertions;
import org.echovantage.util.function.UnsafeRunnable;

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

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walkFileTree;
import static org.echovantage.util.assertion.Assertions.*;

public class Assert2 {
    public static void assertCompletes(final UnsafeRunnable whenCalled) {
        try {
            whenCalled.run();
        } catch (final AssertionError e) {
            throw e;
        } catch (final Throwable t) {
            throw new AssertionError("Runnable did not complete", t);
        }
    }

    public static <T> T assertReturns(final Callable<T> whenCalled) {
        try {
            return whenCalled.call();
        } catch (final AssertionError e) {
            throw e;
        } catch (final Throwable t) {
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

	public static Assertion<Path> existsIn(Path srcRoot, Path testRoot){
		return Assertions.asserts(() -> "exists in " + srcRoot + " and therefore in " + testRoot, p -> {
			final Path sub = srcRoot.relativize(p);
			return Files.exists(testRoot.resolve(sub));
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
		Assertion<Path> exists = existsIn(expected, actual);
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
					final Path sub = expected.relativize(file);
					final Path therePath = actual.resolve(sub);
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
				final int c = hereBuffer[(int)(i % length)];
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

	private static Supplier<String> message(Path sub, long i, int line, int ch){
		return () -> sub + " does not match at byte " + i + " line " + line + " column " + ch;
	}

	private static int read(final InputStream stream, final byte[] buffer, final long limit) throws IOException {
		final int offset = (int)(limit % buffer.length);
		final int count = stream.read(buffer, offset, buffer.length - offset);
		if(count == -1) {
			throw new EOFException();
		}
		return count;
	}
}
