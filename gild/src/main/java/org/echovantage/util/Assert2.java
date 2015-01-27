package org.echovantage.util;

import org.echovantage.util.function.UnsafeRunnable;
import org.junit.Assert;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Callable;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walkFileTree;
import static org.junit.Assert.*;

public class Assert2 {
	public static void assertCompletes(final UnsafeRunnable whenCalled) {
		try {
			whenCalled.run();
		} catch(final AssertionError e) {
			throw e;
		} catch(final Throwable t) {
			throw new AssertionError(description(null, t), t);
		}
	}

	public static <T> T assertCompletes(final Callable<T> whenCalled) {
		try {
			return whenCalled.call();
		} catch(final AssertionError e) {
			throw e;
		} catch(final Throwable t) {
			throw new AssertionError(description(null, t), t);
		}
	}

	public static void assertFails(final UnsafeRunnable whenCalled) {
		try {
			whenCalled.run();
			fail("Expected to fail");
		} catch(final AssertionError e) {
			throw e;
		} catch(final Throwable t) {
			// continue;
		}
	}

	public static void assertFails(final Callable<?> whenCalled) {
		try {
			whenCalled.call();
			fail("Expected to fail");
		} catch(final AssertionError e) {
			throw e;
		} catch(final Throwable t) {
			// continue;
		}
	}

	public static void assertThrown(final Class<? extends Throwable> expected, final Runnable whenCalled) {
		Throwable thrown = null;
		try {
			whenCalled.run();
		} catch(final Throwable t) {
			thrown = t;
		}
		if(!expected.isInstance(thrown)){
			throw new AssertionError("expected:<"+expected+"> but was:<"+(thrown == null ? null : thrown.getClass())+">", thrown);
		}
	}

	public static void assertThrown(final Throwable expected, final Runnable whenCalled) {
		Throwable thrown = null;
		try {
			whenCalled.run();
		} catch(final Throwable t) {
			thrown = t;
		}
		assertEquals(expected, thrown);
	}

	public static void assertThrown(final Class<? extends Throwable> expected, final Callable<?> whenCalled) {
		Throwable thrown = null;
		try {
			whenCalled.call();
		} catch(final Throwable t) {
			thrown = t;
		}
		assertInstance(expected, thrown);
	}

	private static void assertInstance(Class<? extends Throwable> expected, Throwable thrown) {
		Assert.assertEquals(expected, thrown == null ? null : thrown.getClass());
	}

	public static void assertThrown(final Throwable expected, final Callable<?> whenCalled) {
		Throwable thrown = null;
		try {
			whenCalled.call();
		} catch(final Throwable t) {
			thrown = t;
		}
		assertEquals(expected, thrown);
	}

	public static void assertEquals(final Throwable expected, final Throwable actual) {
		if(expected == null && actual == null) {
			return;
		}
		try {
			assertFalse(description(expected, actual), expected == null ^ actual == null);
			Assert.assertEquals(expected.getClass(), actual.getClass());
			Assert.assertEquals(expected.getMessage(), actual.getMessage());
			assertEquals(expected.getCause(), actual.getCause());
		}catch(AssertionError e){
			if(e.getCause() == null && actual != null) {
				e.initCause(actual);
			}
			throw e;
		}
	}

	private static String description(final Object expected, final Object actual) {
		return "expected:<" + nameOf(expected) + "> but was:<" + nameOf(actual) + ">";
	}

	private static String nameOf(final Object obj) {
		return obj == null ? null : obj instanceof Exception ? obj.getClass().getCanonicalName() + ": " + ((Exception)obj).getLocalizedMessage() : (obj instanceof Class ? (Class<?>)obj : obj.getClass()).getCanonicalName();
	}

	/**
	 * Asserts that every file that exists relative to expected also exists
	 * relative to actual.
	 * @param expected the expected path
	 * @param actual the actual path
	 * @throws IOException if the paths cannot be walked
	 */
	public static void containsAll(final Path expected, final Path actual) throws IOException {
		if(Files.exists(expected)) {
			walkFileTree(expected, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					final Path sub = expected.relativize(file);
					assertTrue(sub + " exists in " + expected + ", but not in " + actual, Files.exists(actual.resolve(sub)));
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
					Assert.assertEquals(sub + " is " + hereSize + " bytes in " + expected + ", but " + thereSize + " bytes in " + actual, hereSize, thereSize);
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
				Assert.assertEquals(sub + " does not match at byte " + i + " line " + line + " column " + ch, c, thereBuffer[(int)(i % length)]);
				if(c == '\n') {
					ch = 0;
					line++;
				} else {
					ch++;
				}
			}
		}
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
