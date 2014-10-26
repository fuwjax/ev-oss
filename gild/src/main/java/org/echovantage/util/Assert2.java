package org.echovantage.util;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walkFileTree;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Callable;

import org.junit.Assert;

public class Assert2 {
	/**
	 * Just like a normal {@link Runnable} with throws.
	 * @author fuwjax
	 */
	public interface Runnable {
		/**
		 * Executes the runnable.
		 * @throws Exception if the run cannot complete
		 */
		void run() throws Exception;
	}

	private static Callable<Void> wrap(final Runnable whenCalled) {
		return new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				whenCalled.run();
				return null;
			}
		};
	}

	public static void assertCompletes(final Runnable whenCalled) {
		assertCompletes(wrap(whenCalled));
	}

	public static void assertCompletes(final Callable<?> whenCalled) {
		try {
			whenCalled.call();
		} catch(final AssertionError e) {
			throw e;
		} catch(final Throwable t) {
			fail(description(null, t));
		}
	}

	public static void assertThrown(final Class<? extends Throwable> expected, final Runnable whenCalled) {
		assertThrown(expected, wrap(whenCalled));
	}

	public static void assertThrown(final Throwable expected, final Runnable whenCalled) {
		assertThrown(expected, wrap(whenCalled));
	}

	public static Throwable assertThrown(final Class<? extends Throwable> expected, final Callable<?> whenCalled) {
		try {
			whenCalled.call();
		} catch(final Throwable t) {
			assertTrue(description(expected, t), expected.isInstance(t));
			return t;
		}
		fail(description(expected, null));
		return null;
	}

	public static void assertThrown(final Throwable expected, final Callable<?> whenCalled) {
		assertEquals(expected, assertThrown(expected.getClass(), whenCalled));
	}

	public static void assertEquals(final Throwable expected, final Throwable actual) {
		if(expected == null && actual == null) {
			return;
		}
		assertFalse(description(expected, actual), expected == null ^ actual == null);
		assertTrue(description(expected, actual), expected.getClass().isInstance(actual));
		Assert.assertEquals(expected.getMessage(), actual.getMessage());
		assertEquals(expected.getCause(), actual.getCause());
	}

	private static String description(final Object expected, final Object actual) {
		return "expected:<" + nameOf(expected) + "> but was:<" + nameOf(actual) + ">";
	}

	private static String nameOf(final Object obj) {
		return obj == null ? null : (obj instanceof Class ? (Class<?>)obj : obj.getClass()).getCanonicalName();
	}

	/**
	 * Asserts that every file that exists relative to expected also exists relative to actual.
	 * @param expected the expected path
	 * @param actual the actual path
	 * @throws IOException if the paths cannot be walked
	 */
	public static void containsAll(final Path expected, final Path actual) throws IOException {
		walkFileTree(expected, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				final Path sub = expected.relativize(file);
				assertTrue(sub + " exists in " + expected + ", but not in " + actual, Files.exists(actual.resolve(sub)));
				return super.visitFile(file, attrs);
			}
		});
	}

	/**
	 * Asserts that two paths are deeply byte-equivalent.
	 * @param expected one of the paths
	 * @param actual the other path
	 * @throws IOException if the paths cannot be traversed
	 */
	public static void assertEquals(final Path expected, final Path actual) throws IOException {
		containsAll(expected, actual);
		containsAll(actual, expected);
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
