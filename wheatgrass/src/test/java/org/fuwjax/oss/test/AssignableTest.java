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

import static org.fuwjax.oss.util.assertion.Assertions.assertThat;
import static org.fuwjax.oss.util.assertion.Assertions.is;

import java.io.Serializable;
import java.util.List;

import org.fuwjax.oss.generic.TypeInspector;
import org.fuwjax.oss.util.Types;
import org.junit.Test;

/**
 * Created by fuwjax on 2/22/15.
 */
public class AssignableTest {
    TypeInspector types = new TypeInspector() {
        boolean pBoolean;
        byte pByte;
        short pShort;
        char pChar;
        int pInt;
        long pLong;
        float pFloat;
        double pDouble;
        Boolean oBoolean;
        Byte oByte;
        Short oShort;
        Character oChar;
        Integer oInt;
        Long oLong;
        Float oFloat;
        Double oDouble;
        byte[] pBytes;
        Byte[] oBytes;
        Object object;
        Number number;
        Serializable serializable;
        Comparable<Integer> comparableInt;
        String string;
        CharSequence charSequence;
        List<?> listAny;
        List<? extends CharSequence> listExtendsCharSequence;
        List<String> listString;
        List<CharSequence> listCharSequence;
        List<? super String> listSuperString;
    };

    @Test
    public void testIdentity() {
        types.fields().forEach(t -> assertThat(Types.isAssignable(t, t), is(true)));
    }

    @Test
    public void testWideningPrimitive(){
        assertUnassignable("pBoolean", "pByte");
        assertUnassignable("pBoolean", "pShort");
        assertUnassignable("pBoolean", "pChar");
        assertUnassignable("pBoolean", "pInt");
        assertUnassignable("pBoolean", "pLong");
        assertUnassignable("pBoolean", "pFloat");
        assertUnassignable("pBoolean", "pDouble");

        assertUnassignable("pByte", "pBoolean");
        assertUnassignable("pByte", "pShort");
        assertUnassignable("pByte", "pChar");
        assertUnassignable("pByte", "pInt");
        assertUnassignable("pByte", "pLong");
        assertUnassignable("pByte", "pFloat");
        assertUnassignable("pByte", "pDouble");

        assertUnassignable("pShort", "pBoolean");
        assertAssignable("pShort", "pByte");
        assertUnassignable("pShort", "pChar");
        assertUnassignable("pShort", "pInt");
        assertUnassignable("pShort", "pLong");
        assertUnassignable("pShort", "pFloat");
        assertUnassignable("pShort", "pDouble");

        assertUnassignable("pChar", "pBoolean");
        assertUnassignable("pChar", "pByte");
        assertUnassignable("pChar", "pShort");
        assertUnassignable("pChar", "pInt");
        assertUnassignable("pChar", "pLong");
        assertUnassignable("pChar", "pFloat");
        assertUnassignable("pChar", "pDouble");

        assertUnassignable("pInt", "pBoolean");
        assertAssignable("pInt", "pByte");
        assertAssignable("pInt", "pShort");
        assertAssignable("pInt", "pChar");
        assertUnassignable("pInt", "pLong");
        assertUnassignable("pInt", "pFloat");
        assertUnassignable("pInt", "pDouble");

        assertUnassignable("pLong", "pBoolean");
        assertAssignable("pLong", "pByte");
        assertAssignable("pLong", "pShort");
        assertAssignable("pLong", "pChar");
        assertAssignable("pLong", "pInt");
        assertUnassignable("pLong", "pFloat");
        assertUnassignable("pLong", "pDouble");

        assertUnassignable("pFloat", "pBoolean");
        assertAssignable("pFloat", "pByte");
        assertAssignable("pFloat", "pShort");
        assertAssignable("pFloat", "pChar");
        assertAssignable("pFloat", "pInt");
        assertAssignable("pFloat", "pLong");
        assertUnassignable("pFloat", "pDouble");

        assertUnassignable("pDouble", "pBoolean");
        assertAssignable("pDouble", "pByte");
        assertAssignable("pDouble", "pShort");
        assertAssignable("pDouble", "pChar");
        assertAssignable("pDouble", "pInt");
        assertAssignable("pDouble", "pLong");
        assertAssignable("pDouble", "pFloat");
    }

    @Test
    public void testBoxing(){
        assertUnassignable("oLong", "pInt");
        assertAssignable("oInt", "pInt");
        assertAssignable("object", "pInt");
        assertAssignable("number", "pInt");
        assertAssignable("serializable", "pInt");
        assertAssignable("comparableInt", "pInt");
    }

    @Test
    public void testUnboxing(){
        assertAssignable("pBoolean", "oBoolean");
        assertUnassignable("pBoolean", "oByte");
        assertUnassignable("pBoolean", "oShort");
        assertUnassignable("pBoolean", "oChar");
        assertUnassignable("pBoolean", "oInt");
        assertUnassignable("pBoolean", "oLong");
        assertUnassignable("pBoolean", "oFloat");
        assertUnassignable("pBoolean", "oDouble");

        assertUnassignable("pByte", "oBoolean");
        assertAssignable("pByte", "oByte");
        assertUnassignable("pByte", "oShort");
        assertUnassignable("pByte", "oChar");
        assertUnassignable("pByte", "oInt");
        assertUnassignable("pByte", "oLong");
        assertUnassignable("pByte", "oFloat");
        assertUnassignable("pByte", "oDouble");

        assertUnassignable("pShort", "oBoolean");
        assertAssignable("pShort", "oByte");
        assertAssignable("pShort", "oShort");
        assertUnassignable("pShort", "oChar");
        assertUnassignable("pShort", "oInt");
        assertUnassignable("pShort", "oLong");
        assertUnassignable("pShort", "oFloat");
        assertUnassignable("pShort", "oDouble");

        assertUnassignable("pChar", "oBoolean");
        assertUnassignable("pChar", "oByte");
        assertUnassignable("pChar", "oShort");
        assertAssignable("pChar", "oChar");
        assertUnassignable("pChar", "oInt");
        assertUnassignable("pChar", "oLong");
        assertUnassignable("pChar", "oFloat");
        assertUnassignable("pChar", "oDouble");

        assertUnassignable("pInt", "oBoolean");
        assertAssignable("pInt", "oByte");
        assertAssignable("pInt", "oShort");
        assertAssignable("pInt", "oChar");
        assertAssignable("pInt", "oInt");
        assertUnassignable("pInt", "oLong");
        assertUnassignable("pInt", "oFloat");
        assertUnassignable("pInt", "oDouble");

        assertUnassignable("pLong", "oBoolean");
        assertAssignable("pLong", "oByte");
        assertAssignable("pLong", "oShort");
        assertAssignable("pLong", "oChar");
        assertAssignable("pLong", "oInt");
        assertAssignable("pLong", "oLong");
        assertUnassignable("pLong", "oFloat");
        assertUnassignable("pLong", "oDouble");

        assertUnassignable("pFloat", "oBoolean");
        assertAssignable("pFloat", "oByte");
        assertAssignable("pFloat", "oShort");
        assertAssignable("pFloat", "oChar");
        assertAssignable("pFloat", "oInt");
        assertAssignable("pFloat", "oLong");
        assertAssignable("pFloat", "oFloat");
        assertUnassignable("pFloat", "oDouble");

        assertUnassignable("pDouble", "oBoolean");
        assertAssignable("pDouble", "oByte");
        assertAssignable("pDouble", "oShort");
        assertAssignable("pDouble", "oChar");
        assertAssignable("pDouble", "oInt");
        assertAssignable("pDouble", "oLong");
        assertAssignable("pDouble", "oFloat");
        assertAssignable("pDouble", "oDouble");
    }

    private void assertAssignable(String variable, String expression) {
        assertThat(Types.isAssignable(types.of(variable), types.of(expression)), is(true));
    }

    private void assertUnassignable(String variable, String expression) {
        assertThat(Types.isAssignable(types.of(variable), types.of(expression)), is(false));
    }
}
