package org.echovantage.test;

import static org.echovantage.util.Assert2.assertThrown;
import static org.junit.Assert.fail;

import org.echovantage.util.Assert2;
import org.junit.Test;

public class ExceptionAssertionTest {
	private static final String STATE_THROWN = "expected:<" + IllegalArgumentException.class.getCanonicalName() + "> but was:<" + IllegalStateException.class.getCanonicalName() + ": state>";
	private static final String NULL_STATE_THROWN = "expected:<" + IllegalArgumentException.class.getCanonicalName() + "> but was:<" + IllegalStateException.class.getCanonicalName() + ": null>";
	private static final String NONE_THROWN = "expected:<" + IllegalArgumentException.class.getCanonicalName() + "> but was:<null>";
	private static final String WRONG_MESSAGE = "expected:<[bob]> but was:<[hope]>";
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
			Assert2.assertEquals(new AssertionError(STATE_THROWN), e);
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
			Assert2.assertEquals(new AssertionError(NONE_THROWN), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionInstance() {
		try {
			assertThrown(new IllegalArgumentException(), () -> state(null, null));
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(NULL_STATE_THROWN), e);
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
			Assert2.assertEquals(new AssertionError(WRONG_MESSAGE), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionCauseMessage() {
		try {
			assertThrown(new IllegalArgumentException("bob", new IllegalStateException("bob")), () -> arg("bob", new IllegalStateException("hope")));
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(WRONG_MESSAGE), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionMissingMessage() {
		try {
			assertThrown(new IllegalArgumentException("bob", new IllegalStateException("bob")), () -> arg("bob", new IllegalStateException()));
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(MISSING_MESSAGE), e);
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionMissingCause() {
		try {
			assertThrown(new IllegalArgumentException("bob", new IllegalArgumentException("right")), () -> arg("bob", null));
		} catch(final AssertionError e) {
			Assert2.assertEquals(new AssertionError(MISSING_CAUSE), e);
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
