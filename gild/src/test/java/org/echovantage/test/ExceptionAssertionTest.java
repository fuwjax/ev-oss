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

import org.echovantage.util.Assert2;
import org.junit.Test;

import static org.echovantage.util.Assert2.assertThrown;
import static org.echovantage.util.assertion.Assertions.assertThat;
import static org.echovantage.util.assertion.Assertions.isException;
import static org.junit.Assert.*;

public class ExceptionAssertionTest {
	private static final String STATE_THROWN = "expected:<java.lang.IllegalArgumentException> but was:<java.lang.IllegalStateException>";
	private static final String STATE_THROWN_FULL = "expected:<instance of java.lang.IllegalArgumentException> but was:<java.lang.IllegalStateException(null, null)>";
	private static final String NONE_THROWN = "expected:<not null and direct instance of java.lang.IllegalArgumentException> but was:<null>";
	private static final String NONE_MSG_THROWN = "expected:<instance of java.lang.IllegalArgumentException and message is [null] and cause is [null]> but was:<null>";
	private static final String MISSING_MESSAGE = "expected:<bob> but was:<null>";
	private static final String MISSING_CAUSE = "expected:<instance of java.lang.IllegalArgumentException> but was:<null>";
	private static final String BOB_HOPE = "expected:<bob> but was:<hope>";

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
			assertThat(e, isException(new AssertionError(STATE_THROWN, new IllegalStateException("state"))));
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
			assertThat(e, isException(new AssertionError(STATE_THROWN_FULL, new IllegalStateException())));
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
			assertThat(e, isException(new AssertionError(BOB_HOPE, new IllegalArgumentException("hope"))));
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionCauseMessage() {
		try {
			assertThrown(new IllegalArgumentException("bob", new IllegalStateException("bob")), () -> arg("bob", new IllegalStateException("hope")));
		} catch(final AssertionError e) {
			assertThat(e, isException(new AssertionError(BOB_HOPE, new IllegalArgumentException("bob", new IllegalStateException("hope")))));
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionMissingMessage() {
		try {
			assertThrown(new IllegalArgumentException("bob", new IllegalStateException("bob")), () -> arg("bob", new IllegalStateException()));
		} catch(final AssertionError e) {
			assertThat(e, isException(new AssertionError(MISSING_MESSAGE, new IllegalArgumentException("bob", new IllegalStateException()))));
			return;
		}
		fail("the closure should not throw an exception");
	}

	@Test
	public void testThrownUnexpectedExceptionMissingCause() {
		try {
			assertThrown(new IllegalArgumentException("bob", new IllegalArgumentException("right")), () -> arg("bob", null));
		} catch(final AssertionError e) {
			assertThat(e, isException(new AssertionError(MISSING_CAUSE, new IllegalArgumentException("bob"))));
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
