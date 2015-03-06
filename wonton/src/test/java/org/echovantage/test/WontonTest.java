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

import org.echovantage.util.assertion.Assertion;
import org.echovantage.util.assertion.Assertions;
import org.echovantage.wonton.Path;
import org.echovantage.wonton.Wonton;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.echovantage.util.assertion.Assertions.assertThat;
import static org.echovantage.util.assertion.Assertions.failsToReturnWith;
import static org.echovantage.util.assertion.Assertions.failsWith;
import static org.echovantage.util.assertion.Assertions.isA;
import static org.echovantage.wonton.WontonFactory.FACTORY;
import static org.junit.Assert.*;

public class WontonTest {
    @Test
    public void testNull() {
        final Wonton wonton = FACTORY.wontonOf((Object) null);
        assertEquals(Wonton.VOID, wonton.type());
        assertNull(wonton.value());
        assertNull(wonton.as(Wonton.ARRAY));
        assertNull(wonton.as(Wonton.BOOLEAN));
        assertNull(wonton.as(Wonton.NUMBER));
        assertNull(wonton.as(Wonton.STRUCT));
        assertNull(wonton.as(Wonton.ARRAY));
    }

    @Test
    public void testTrue() {
        final Wonton wonton = FACTORY.wontonOf(true);
        assertEquals(Wonton.BOOLEAN, wonton.type());
        assertTrue((Boolean) wonton.value());
        assertWrongType(wonton, Wonton.ARRAY);
        assertEquals(true, wonton.as(Wonton.BOOLEAN));
        assertWrongType(wonton, Wonton.NUMBER);
        assertWrongType(wonton, Wonton.STRUCT);
        assertWrongType(wonton, Wonton.STRING);
    }

    @Test
    public void testFalse() {
        final Wonton wonton = FACTORY.wontonOf(false);
        assertEquals(Wonton.BOOLEAN, wonton.type());
        assertFalse((Boolean) wonton.value());
        assertWrongType(wonton, Wonton.ARRAY);
        assertEquals(false, wonton.as(Wonton.BOOLEAN));
        assertWrongType(wonton, Wonton.NUMBER);
        assertWrongType(wonton, Wonton.STRUCT);
        assertWrongType(wonton, Wonton.STRING);
    }

    @Test
    public void testDouble() {
        final Wonton wonton = FACTORY.wontonOf(5.3);
        assertEquals(Wonton.NUMBER, wonton.type());
        assertEquals(5.3, wonton.value());
        assertWrongType(wonton, Wonton.ARRAY);
        assertWrongType(wonton, Wonton.BOOLEAN);
        assertEquals(5.3, wonton.as(Wonton.NUMBER));
        assertWrongType(wonton, Wonton.STRUCT);
        assertWrongType(wonton, Wonton.STRING);
    }

    @Test
    public void testInteger() {
        final Wonton wonton = FACTORY.wontonOf(5);
        assertEquals(Wonton.NUMBER, wonton.type());
        assertEquals(5, wonton.value());
        assertWrongType(wonton, Wonton.ARRAY);
        assertWrongType(wonton, Wonton.BOOLEAN);
        assertEquals(5, wonton.as(Wonton.NUMBER));
        assertWrongType(wonton, Wonton.STRUCT);
        assertWrongType(wonton, Wonton.STRING);
    }

    @Test
    public void testString() {
        final Wonton wonton = FACTORY.wontonOf("Hello, World!");
        assertEquals(Wonton.STRING, wonton.type());
        assertEquals("Hello, World!", wonton.value());
        assertWrongType(wonton, Wonton.ARRAY);
        assertWrongType(wonton, Wonton.BOOLEAN);
        assertWrongType(wonton, Wonton.NUMBER);
        assertWrongType(wonton, Wonton.STRUCT);
        assertEquals("Hello, World!", wonton.as(Wonton.STRING));
    }

    @Test
    public void testList() {
        final Wonton wonton = FACTORY.wontonOf(asList(1, 2, 3));
        assertEquals(Wonton.ARRAY, wonton.type());
        assertEquals(asList(1, 2, 3), wonton.as(Wonton.NATURAL));
        // no longer true
        //assertEquals(wonton.value(), wonton.as(Wonton.ARRAY));
        assertWrongType(wonton, Wonton.BOOLEAN);
        assertWrongType(wonton, Wonton.NUMBER);
        assertWrongType(wonton, Wonton.STRUCT);
        assertWrongType(wonton, Wonton.STRING);
        assertThat(wonton.get(Path.path("[0]")), isWonton(1));
        assertThat(wonton.get("0"), isWonton(1));
        assertNoKey(() -> wonton.get("12"));
        assertNoKey(() -> wonton.get(Path.path("[9]")));
        assertNoKey(() -> wonton.get("bob"));
        wonton.accept((key, value) -> assertThat(wonton.get(key), isWonton(value)));
    }

    public static Assertion<? super Wonton> isWonton(Object expectedValue) {
        return Assertions.asserts(() -> "is wonton of "+expectedValue, w -> Objects.equals(w.as(Wonton.NATURAL), expectedValue));
    }

    public static Assertion<? super Wonton> isWonton(Wonton expectedValue) {
        return isWonton(expectedValue.as(Wonton.NATURAL));
    }

    @Test
    public void testPrimitiveArray() {
        final Wonton wonton = FACTORY.wontonOf(new int[]{1, 2, 3});
        assertEquals(Wonton.ARRAY, wonton.type());
        assertEquals(asList(1, 2, 3), wonton.as(Wonton.NATURAL));
        // no longer true
        //assertEquals(wonton.value(), wonton.as(Wonton.ARRAY));
        assertWrongType(wonton, Wonton.BOOLEAN);
        assertWrongType(wonton, Wonton.NUMBER);
        assertWrongType(wonton, Wonton.STRUCT);
        assertWrongType(wonton, Wonton.STRING);
    }

    @Test
    public void testObjectArray() {
        final Wonton wonton = FACTORY.wontonOf(new Object[]{1, 2, 3});
        assertEquals(Wonton.ARRAY, wonton.type());
        assertEquals(asList(1, 2, 3), wonton.as(Wonton.NATURAL));
        // no longer true
        //assertEquals(wonton.value(), wonton.as(Wonton.ARRAY));
        assertWrongType(wonton, Wonton.BOOLEAN);
        assertWrongType(wonton, Wonton.NUMBER);
        assertWrongType(wonton, Wonton.STRUCT);
        assertWrongType(wonton, Wonton.STRING);
    }

    @Test
    public void testMap() {
        final Wonton wonton = FACTORY.wontonOf(singletonMap("bob", "hope"));
        assertEquals(Wonton.STRUCT, wonton.type());
        assertEquals(singletonMap("bob", "hope"), wonton.as(Wonton.NATURAL));
        assertWrongType(wonton, Wonton.ARRAY);
        assertWrongType(wonton, Wonton.BOOLEAN);
        assertWrongType(wonton, Wonton.NUMBER);
        // no longer true
        //assertEquals(wonton.value(), wonton.asStruct());
        assertWrongType(wonton, Wonton.STRING);
        assertThat(wonton.get(Path.path("bob")), isWonton("hope"));
        assertThat(wonton.get(Path.path("[bob]")), isWonton("hope"));
        wonton.accept((key, value) -> assertThat(wonton.get(key), isWonton(value)));
    }

    private void assertWrongType(final Wonton wonton, Wonton.Type<?> type) {
        assertThat(() -> wonton.as(type), failsToReturnWith(isA(ClassCastException.class)));
    }

    private void assertNoKey(final Runnable runner) {
        assertThat(runner, failsWith(isA(Wonton.NoSuchPathException.class)));
    }

    @Test
    public void testDeep() {
        final Wonton wonton = FACTORY.wontonOf(singletonMap("root", asList(singletonMap("bob", "hope"), singletonMap("bob", "newhart"))));
        assertEquals(Wonton.STRUCT, wonton.type());
        assertWrongType(wonton, Wonton.ARRAY);
        assertWrongType(wonton, Wonton.BOOLEAN);
        assertWrongType(wonton, Wonton.NUMBER);
        // no longer true
        //assertEquals(wonton.value(), wonton.asStruct());
        assertWrongType(wonton, Wonton.STRING);
        assertThat(wonton.get(Path.path("root")), isWonton(asList(singletonMap("bob", "hope"), singletonMap("bob", "newhart"))));
        assertThat(wonton.get(Path.path("root[0]")), isWonton(singletonMap("bob", "hope")));
        assertThat(wonton.get(Path.path("root[0].bob")), isWonton("hope"));
        assertThat(wonton.get(Path.path("[root][1][bob]")), isWonton("newhart"));
        assertThat(wonton.get("root").get("1").get("bob"), isWonton("newhart"));
        assertThat(wonton.get(Path.path("root.0.bob")), isWonton("hope"));
        wonton.accept((key, value) -> assertThat(wonton.get(key), isWonton(value)));
    }
}
