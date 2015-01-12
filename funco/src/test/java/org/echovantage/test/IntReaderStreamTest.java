package org.echovantage.test;

import org.echovantage.util.io.IntReader;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by fuwjax on 1/11/15.
 */
public class IntReaderStreamTest {
    @Test
    public void testIntReaderStream(){
        IntReader reader = IntReader.codepoints("hello");
        assertEquals(5, reader.stream().count());

        reader = IntReader.codepoints("hello");
        assertEquals("ehllo", reader.stream().sorted().mapToObj(Character::toChars).map(String::new).collect(Collectors.joining()));

        reader = IntReader.codepoints("hello");
        assertEquals("hello", reader.stream().boxed().map(Character::toChars).map(String::new).collect(Collectors.joining()));
    }
}
