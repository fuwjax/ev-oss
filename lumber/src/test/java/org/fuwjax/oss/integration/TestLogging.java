package org.fuwjax.oss.integration;

import org.fuwjax.oss.sample.SampleClass;
import org.junit.Test;

public class TestLogging {
	@Test
	public void testLogging() {
		new SampleClass().doSomething("bob", 1);
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testCrazyLogging() {
		new SampleClass().doSomethingCrazy(1, "bob", "hope");
		new SampleClass().doSomethingCrazy(4, "duck", "duck", "duck", "goose", "gander");
		new SampleClass().doSomethingCrazy(2, "bob", "hope");
	}
}
