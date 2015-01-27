package org.echovantage.test;

import org.echovantage.util.Assert2;
import org.junit.ComparisonFailure;
import org.junit.Test;

import static org.echovantage.util.Assert2.assertThrown;
import static org.junit.Assert.*;

public class ExceptionAssertionTest {
	private static final String STATE_THROWN = "expected:<class " + IllegalArgumentException.class.getCanonicalName() + "> but was:<class " + IllegalStateException.class.getCanonicalName() + ">";
	private static final String NONE_THROWN = "expected:<class " + IllegalArgumentException.class.getCanonicalName() + "> but was:<null>";
	private static final String NONE_MSG_THROWN = "expected:<" + IllegalArgumentException.class.getCanonicalName() + ": null> but was:<null>";
	private static final String MISSING_MESSAGE = "expected:<bob> but was:<null>";
	private static final String MISSING_CAUSE = "expected:<" + IllegalArgumentException.class.getCanonicalName() + ": right> but was:<null>";

	@Test
	public void testNoThrownException() {
		try {
			assertThrown(IllegalArgumentException.class, () -> 5);
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(NONE_THROWN), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedException() {
		try {
			assertThrown(IllegalArgumentException.class, () -> state("state", null));
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(STATE_THROWN, new IllegalStateException("state")), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownExpectedException() {
		assertThrown(IllegalArgumentException.class, () -> arg("arg", null));
	}

	@Test
	public void testNoThrownExceptionInstance() {
		try {
			assertThrown(new IllegalArgumentException(), () -> 5);
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(NONE_MSG_THROWN), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionInstance() {
		try {
			assertThrown(new IllegalArgumentException(), () -> state(null, null));
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(STATE_THROWN, new IllegalStateException()), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownExpectedExceptionInstance() {
		assertThrown(new IllegalArgumentException(), () -> arg(null, null));
	}

	@Test
	public void testThrownExpectedExceptionInstanceMessage() {
		assertThrown(new IllegalArgumentException("test123"), () -> arg("test123", null));
	}

	@Test
	public void testThrownExpectedExceptionInstanceCauseMessage() {
		assertThrown(new IllegalArgumentException("hello", new IllegalStateException("world")), () -> arg("hello", new IllegalStateException("world")));
	}

	@Test
	public void testThrownUnexpectedExceptionMessage() {
		try {
			assertThrown(new IllegalArgumentException("bob"), () -> arg("hope", null));
		} catch(final AssertionError e) {
			ComparisonFailure failure = new ComparisonFailure("", "bob", "hope");
			failure.initCause(new IllegalArgumentException("hope", null));
			Assert2.assertEquals(failure, e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionCauseMessage() {
		try {
			assertThrown(new IllegalArgumentException("bob", new IllegalStateException("bob")), () -> arg("bob", new IllegalStateException("hope")));
		} catch(final AssertionError e) {
			ComparisonFailure failure = new ComparisonFailure("", "bob", "hope");
			failure.initCause(new IllegalStateException("hope", null));
			Assert2.assertEquals(failure, e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionMissingMessage() {
		try {
			assertThrown(new IllegalArgumentException("bob", new IllegalStateException("bob")), () -> arg("bob", new IllegalStateException()));
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(MISSING_MESSAGE, new IllegalStateException()), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionMissingCause() {
		try {
			assertThrown(new IllegalArgumentException("bob", new IllegalArgumentException("right")), () -> arg("bob", null));
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(MISSING_CAUSE, new IllegalArgumentException("bob")), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	public void state(final String message, final Throwable cause) {
		throw new IllegalStateException(message, cause);
	}

	public void arg(final String message, final Throwable cause) {
		throw new IllegalArgumentException(message, cause);
	}
}
