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

import org.fuwjax.oss.util.assertion.Assertions;
import org.fuwjax.oss.util.parser.ParseState;
import org.fuwjax.oss.wonton.Path;
import org.fuwjax.oss.wonton.Wonton;
import org.fuwjax.oss.wonton.WontonFactory;
import org.fuwjax.oss.wonton.WontonParser;
import org.fuwjax.oss.wonton.WontonSerial;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;

import static org.fuwjax.oss.test.WontonTest.isWonton;
import static org.fuwjax.oss.util.assertion.Assertions.assertThat;
import static java.util.Collections.*;
import static org.fuwjax.oss.util.assertion.Assertions.failsToReturnWith;
import static org.fuwjax.oss.util.assertion.Assertions.is;
import static org.fuwjax.oss.util.assertion.Assertions.isA;
import static org.fuwjax.oss.util.assertion.Assertions.isAny;
import static org.fuwjax.oss.util.assertion.Assertions.isAssignableFrom;
import static org.fuwjax.oss.util.assertion.Assertions.isException;
import static org.junit.Assert.*;

public class SerializationTest {
    @Test
    public void testGet() throws Exception {
        assertGet("{a:1,b:2}", "a", 1);
        assertGet("{a:[1,2,3],b:2}", "a[0]", 1);
        assertGet("{a:[{x:1,y:2},2,3],b:2}", "a[0].x", 1);
        assertGet("{a:[[0,{x:1,y:2}],2,3],b:2}", "a[0][1].x", 1);
        assertGet("[[0,{x:1,y:2}],2,3]", "[0][1].x", 1);
    }

    @Test
    public void testFailure() throws Exception {
        failParse("{a:1,:2}", "Key expected, was ':' at line 1 column 6\nwhile parsing object, was '{' at line 1 column 1");
        failParse("{a:1,b:}", "Value expected, was '}' at line 1 column 8\nwhile parsing object, was '{' at line 1 column 1");
        failParse("{a:1,b:2", "Expected '}', was EOF at line 1 column 9\nwhile parsing object, was '{' at line 1 column 1");
        failParse("{a:1:b:2}", "Expected '}', was ':' at line 1 column 5\nwhile parsing object, was '{' at line 1 column 1");
        failParse("{a:{z,1},b:2}", "Expected ':', was ',' at line 1 column 6\nwhile parsing object, was '{' at line 1 column 4\nwhile parsing object, was '{' at line 1 column 1");
        failParse("{{z:1}:3,b:2}", "Key expected, was '{' at line 1 column 2\nwhile parsing object, was '{' at line 1 column 1");
    }

    @Test
    public void testParse() throws Exception {
        assertParse(singletonMap("x", 1), "{\n\tx\n\t\t:\n\t\t\t1\n}");
        assertParse(123, "123");
        assertParse(null, "null");
        assertParse(true, "true");
        assertParse(false, "false");
        assertParse(0.0, "0.0");
        assertParse(1234567890, "1234567890");
        assertParse(9876543210L, "9876543210");
        assertParse(-9876543210L, "-9876543210");
        assertParse(emptyMap(), "{}");
        assertParse(emptyList(), "[]");
        assertParse("hi mom", "\"hi mom\"");
        assertParse("hello\nworld", "\"hello\\nworld\"");
        assertParse("hello\\world", "\"hello\\\\world\"");
        assertParse("𐌀", "\"𐌀\"");
    }

    @Test
    public void testSerial() throws Exception {
        assertSerial(123L, "123");
        assertSerial(null, "null");
        assertSerial(true, "true");
        assertSerial(false, "false");
        assertSerial(0.0, "0.0");
        assertSerial(emptyMap(), "{}");
        assertSerial(emptyList(), "[]");
        assertSerial("hi mom", "\"hi mom\"");
        assertSerial("\ud800\udf00", "\"\\uD800\\uDF00\"");
        assertSerial("𐌀", "\"\\uD800\\uDF00\"");
        assertSerial("hello\nworld", "\"hello\\nworld\"");
        assertSerial(Arrays.asList("hi mom", "hello\nworld"), "[\"hi mom\",\"hello\\nworld\"]");
        assertSerial("hello\\world", "\"hello\\\\world\"");
    }

    private static void assertGet(final String input, final String property, final Object expected) throws Exception {
        assertThat(new WontonParser(input).parse().get(Path.path(property)), isWonton(expected));
    }

    private static void assertParse(final Object value, final String json) throws Exception {
        assertThat(parse(json), isWonton(value));
    }

    private static void failParse(final String json, final String message) {
        assertThat(() -> parse(json), failsToReturnWith(isException(isAssignableFrom(ParseException.class), is(message))));
    }

    private static Wonton parse(final String json) throws Exception {
        return new WontonParser(json).parse();
    }

    private static void assertSerial(final Object value, final String json) {
        assertEquals(json, WontonSerial.toString(WontonFactory.FACTORY.wontonOf(value)));
    }
}
