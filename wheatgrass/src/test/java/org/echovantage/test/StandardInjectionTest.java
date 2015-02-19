package org.echovantage.test;

import static org.echovantage.util.assertion.Assertions.assertThat;
import static org.echovantage.util.assertion.Assertions.is;

import org.echovantage.inject.Injector;
import org.echovantage.sample.SampleFieldInject;
import org.echovantage.sample.SampleModule;
import org.echovantage.sample.SampleConstructorInject;
import org.junit.Test;

public class StandardInjectionTest {
	@Test
	public void testDirectLookup() {
		final Injector root = new Injector(new SampleModule());
		assertThat(root.get(String.class), is("SampleModule"));
	}

    @Test
    public void testCreateAndInjectConstructor() {
        final Injector root = new Injector(new SampleModule());
        assertThat(root.get(SampleConstructorInject.class), is(new SampleConstructorInject(4, "SampleModule")));
    }

    @Test
    public void testCreateAndInjectFields() {
        final Injector root = new Injector(new SampleModule());
        assertThat(root.get(SampleFieldInject.class), is(new SampleFieldInject(4, "SampleModule")));
    }
}
