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

import org.echovantage.util.io.IntReader;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.echovantage.util.assertion.Assertions.assertThat;
import static org.echovantage.util.assertion.Assertions.is;


/**
 * Created by fuwjax on 1/11/15.
 */
public class IntReaderStreamTest {
    @Test
    public void testIntReaderStream(){
        IntReader reader = IntReader.codepoints("hello");
        assertThat(5L, is(reader.stream().count()));

        reader = IntReader.codepoints("hello");
        assertThat("ehllo", is(reader.stream().sorted().mapToObj(Character::toChars).map(String::new).collect(Collectors.joining())));

        reader = IntReader.codepoints("hello");
        assertThat("hello", is(reader.stream().boxed().map(Character::toChars).map(String::new).collect(Collectors.joining())));
    }
}
