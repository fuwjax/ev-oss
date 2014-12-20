package org.echovantage.test;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.WontonFactory;
import org.echovantage.wonton.WontonParser;
import org.echovantage.wonton.WontonSerial;
import org.junit.Test;

import java.util.Arrays;

import static java.util.Collections.*;
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
		assertEquals(WontonFactory.FACTORY.wontonOf(expected), new WontonParser(input).parse().get(Wonton.path(property)));
	}

	private static void assertParse(final Object value, final String json) throws Exception {
		assertEquals(WontonFactory.FACTORY.wontonOf(value), parse(json));
	}

	private static void failParse(final String json, final String message) {
		try {
			parse(json);
			fail("Did not throw an exception");
		} catch(Exception e) {
			assertEquals(message, e.getMessage());
		}
	}

	private static Wonton parse(final String json) throws Exception {
		return new WontonParser(json).parse();
	}

	private static void assertSerial(final Object value, final String json) {
		assertEquals(json, WontonSerial.toString(WontonFactory.FACTORY.wontonOf(value)));
	}
}
