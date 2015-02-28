package org.echovantage.test;

import org.echovantage.util.Annotations;
import org.echovantage.util.assertion.Assertions;
import org.junit.Test;

import java.util.Collections;

import static org.echovantage.util.assertion.Assertions.assertThat;
import static org.echovantage.util.assertion.Assertions.is;

/**
 * Created by fuwjax on 2/27/15.
 */
public class AnnotationsTest {
    @Test
    public void testSimpleAnnotation() throws NoSuchMethodException {
        Test real = AnnotationsTest.class.getDeclaredMethod("testSimpleAnnotation").getAnnotation(Test.class);
        Test fake = Annotations.of(Test.class);
        assertThat(fake, is(real));
        assertThat(fake.hashCode(), is(real.hashCode()));
        assertThat(fake.toString().length(), is(real.toString().length()));
        assertThat(fake.expected(), is(real.expected()));
        assertThat(fake.timeout(), is(real.timeout()));
        assertThat(fake.annotationType(), is(real.annotationType()));
    }

    @Test(timeout = 1000)
    public void testValuesAnnotation() throws NoSuchMethodException {
        Test real = AnnotationsTest.class.getDeclaredMethod("testValuesAnnotation").getAnnotation(Test.class);
        Test fake = Annotations.of(Test.class, Collections.singletonMap("timeout", 1000L));
        assertThat(fake, is(real));
        assertThat(fake.hashCode(), is(real.hashCode()));
        assertThat(fake.toString().length(), is(real.toString().length()));
        assertThat(fake.expected(), is(real.expected()));
        assertThat(fake.timeout(), is(real.timeout()));
        assertThat(fake.annotationType(), is(real.annotationType()));
    }
}
