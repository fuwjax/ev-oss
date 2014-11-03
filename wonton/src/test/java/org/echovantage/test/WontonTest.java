package org.echovantage.test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.echovantage.wonton.StandardWontonFactory.FACTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.echovantage.wonton.StandardType;
import org.echovantage.wonton.Wonton;
import org.junit.Test;

public class WontonTest {
	@Test
	public void testNull() {
		final Wonton wonton = FACTORY.create(null);
		assertEquals(StandardType.NULL, wonton.type());
		assertNull(wonton.value());
		assertEquals(emptyList(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(0.0, wonton.asNumber());
		assertEquals(emptyMap(), wonton.asObject());
		assertEquals("", wonton.asString());
		assertEquals(Wonton.NULL, wonton);
	}

	@Test
	public void testTrue() {
		final Wonton wonton = FACTORY.create(true);
		assertEquals(StandardType.BOOLEAN, wonton.type());
		assertTrue((Boolean)wonton.value());
		assertEquals(emptyList(), wonton.asArray());
		assertEquals(true, wonton.asBoolean());
		assertEquals(0.0, wonton.asNumber());
		assertEquals(emptyMap(), wonton.asObject());
		assertEquals("", wonton.asString());
		assertEquals(Wonton.TRUE, wonton);
	}

	@Test
	public void testFalse() {
		final Wonton wonton = FACTORY.create(false);
		assertEquals(StandardType.BOOLEAN, wonton.type());
		assertFalse((Boolean)wonton.value());
		assertEquals(emptyList(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(0.0, wonton.asNumber());
		assertEquals(emptyMap(), wonton.asObject());
		assertEquals("", wonton.asString());
		assertEquals(Wonton.FALSE, wonton);
	}

	@Test
	public void testDouble() {
		final Wonton wonton = FACTORY.create(5.3);
		assertEquals(StandardType.NUMBER, wonton.type());
		assertEquals(5.3, wonton.value());
		assertEquals(emptyList(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(5.3, wonton.asNumber());
		assertEquals(emptyMap(), wonton.asObject());
		assertEquals("", wonton.asString());
	}

	@Test
	public void testInteger() {
		final Wonton wonton = FACTORY.create(5);
		assertEquals(StandardType.NUMBER, wonton.type());
		assertEquals(5, wonton.value());
		assertEquals(emptyList(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(5, wonton.asNumber());
		assertEquals(emptyMap(), wonton.asObject());
		assertEquals("", wonton.asString());
	}

	@Test
	public void testString() {
		final Wonton wonton = FACTORY.create("Hello, World!");
		assertEquals(StandardType.STRING, wonton.type());
		assertEquals("Hello, World!", wonton.value());
		assertEquals(emptyList(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(0.0, wonton.asNumber());
		assertEquals(emptyMap(), wonton.asObject());
		assertEquals("Hello, World!", wonton.asString());
	}

	@Test
	public void testList() {
		final Wonton wonton = FACTORY.create(asList(1, 2, 3));
		assertEquals(StandardType.ARRAY, wonton.type());
		assertEquals(asList(FACTORY.create(1), FACTORY.create(2), FACTORY.create(3)), wonton.value());
		assertEquals(wonton.value(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(0.0, wonton.asNumber());
		assertEquals(emptyMap(), wonton.asObject());
		assertEquals("", wonton.asString());
		assertEquals(FACTORY.create(1), wonton.get("[0]"));
		assertEquals(FACTORY.create(1), wonton.get("0"));
		assertEquals(FACTORY.create(null), wonton.get("12"));
		assertEquals(FACTORY.create(null), wonton.get("[9]"));
		assertEquals(FACTORY.create(null), wonton.get("bob"));
		wonton.accept((key, value) -> assertEquals(value, wonton.get(key)));
	}

	@Test
	public void testArray() {
		final Wonton wonton = FACTORY.create(new int[] { 1, 2, 3 });
		assertEquals(StandardType.ARRAY, wonton.type());
		assertEquals(asList(FACTORY.create(1), FACTORY.create(2), FACTORY.create(3)), wonton.value());
		assertEquals(wonton.value(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(0.0, wonton.asNumber());
		assertEquals(emptyMap(), wonton.asObject());
		assertEquals("", wonton.asString());
	}

	@Test
	public void testObjectArray() {
		final Wonton wonton = FACTORY.create(new Object[] { 1, 2, 3 });
		assertEquals(StandardType.ARRAY, wonton.type());
		assertEquals(asList(FACTORY.create(1), FACTORY.create(2), FACTORY.create(3)), wonton.value());
		assertEquals(wonton.value(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(0.0, wonton.asNumber());
		assertEquals(emptyMap(), wonton.asObject());
		assertEquals("", wonton.asString());
	}

	@Test
	public void testMap() {
		final Wonton wonton = FACTORY.create(singletonMap("bob", "hope"));
		assertEquals(StandardType.OBJECT, wonton.type());
		assertEquals(singletonMap("bob", FACTORY.create("hope")), wonton.value());
		assertEquals(emptyList(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(0.0, wonton.asNumber());
		assertEquals(wonton.value(), wonton.asObject());
		assertEquals("", wonton.asString());
		assertEquals(FACTORY.create("hope"), wonton.get("bob"));
		assertEquals(FACTORY.create("hope"), wonton.get("[bob]"));
		wonton.accept((key, value) -> assertEquals(value, wonton.get(key)));
	}

	@Test
	public void testDeep() {
		final Wonton wonton = FACTORY.create(singletonMap("root", asList(singletonMap("bob", "hope"), singletonMap("bob", "newhart"))));
		assertEquals(StandardType.OBJECT, wonton.type());
		assertEquals(emptyList(), wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertEquals(0.0, wonton.asNumber());
		assertEquals(wonton.value(), wonton.asObject());
		assertEquals("", wonton.asString());
		assertEquals(FACTORY.create("hope"), wonton.get("root[0].bob"));
		assertEquals(FACTORY.create("newhart"), wonton.get("[root][1][bob]"));
		assertEquals(FACTORY.create("hope"), wonton.get("root.0.bob"));
		wonton.accept((key, value) -> assertEquals(value, wonton.get(key)));
	}
}
