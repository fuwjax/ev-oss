/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
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
package org.fuwjax.oss.test;

import org.fuwjax.oss.util.Annotations;
import org.fuwjax.oss.util.assertion.Assertions;
import org.junit.Test;

import java.util.Collections;

import static org.fuwjax.oss.util.assertion.Assertions.assertThat;
import static org.fuwjax.oss.util.assertion.Assertions.is;

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
