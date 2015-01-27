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
