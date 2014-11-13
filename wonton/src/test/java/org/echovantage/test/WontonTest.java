package org.echovantage.test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.echovantage.wonton.standard.StandardFactory.FACTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.echovantage.wonton.Wonton;
import org.junit.Test;

public class WontonTest {
	@Test
	public void testNull() {
		final Wonton wonton = FACTORY.wrap(null);
		assertEquals(Wonton.Type.VOID, wonton.type());
		assertNull(wonton.value());
		assertNull(wonton.asArray());
		assertNull(wonton.asBoolean());
		assertNull(wonton.asNumber());
		assertNull(wonton.asStruct());
		assertNull(wonton.asString());
	}

	@Test
	public void testTrue() {
		final Wonton wonton = FACTORY.wrap(true);
		assertEquals(Wonton.Type.BOOLEAN, wonton.type());
		assertTrue((Boolean)wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertEquals(true, wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testFalse() {
		final Wonton wonton = FACTORY.wrap(false);
		assertEquals(Wonton.Type.BOOLEAN, wonton.type());
		assertFalse((Boolean)wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testDouble() {
		final Wonton wonton = FACTORY.wrap(5.3);
		assertEquals(Wonton.Type.NUMBER, wonton.type());
		assertEquals(5.3, wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertEquals(5.3, wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testInteger() {
		final Wonton wonton = FACTORY.wrap(5);
		assertEquals(Wonton.Type.NUMBER, wonton.type());
		assertEquals(5, wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertEquals(5, wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testString() {
		final Wonton wonton = FACTORY.wrap("Hello, World!");
		assertEquals(Wonton.Type.STRING, wonton.type());
		assertEquals("Hello, World!", wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertEquals("Hello, World!", wonton.asString());
	}

	@Test
	public void testList() {
		final Wonton wonton = FACTORY.wrap(asList(1, 2, 3));
		assertEquals(Wonton.Type.ARRAY, wonton.type());
		assertEquals(asList(FACTORY.wrap(1), FACTORY.wrap(2), FACTORY.wrap(3)), wonton.value());
		assertEquals(wonton.value(), wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
		assertEquals(FACTORY.wrap(1), wonton.get("[0]"));
		assertEquals(FACTORY.wrap(1), wonton.get("0"));
		assertNoKey(() -> wonton.get("12"));
		assertNoKey(() -> wonton.get("[9]"));
		assertNoKey(() -> wonton.get("bob"));
		wonton.accept((key, value) -> assertEquals(value, wonton.get(key)));
	}

	@Test
	public void testArray() {
		final Wonton wonton = FACTORY.wrap(new int[] { 1, 2, 3 });
		assertEquals(Wonton.Type.ARRAY, wonton.type());
		assertEquals(asList(FACTORY.wrap(1), FACTORY.wrap(2), FACTORY.wrap(3)), wonton.value());
		assertEquals(wonton.value(), wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testObjectArray() {
		final Wonton wonton = FACTORY.wrap(new Object[] { 1, 2, 3 });
		assertEquals(Wonton.Type.ARRAY, wonton.type());
		assertEquals(asList(FACTORY.wrap(1), FACTORY.wrap(2), FACTORY.wrap(3)), wonton.value());
		assertEquals(wonton.value(), wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testMap() {
		final Wonton wonton = FACTORY.wrap(singletonMap("bob", "hope"));
		assertEquals(Wonton.Type.STRUCT, wonton.type());
		assertEquals(singletonMap("bob", FACTORY.wrap("hope")), wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertEquals(wonton.value(), wonton.asStruct());
		assertWrongType(() -> wonton.asString());
		assertEquals(FACTORY.wrap("hope"), wonton.get("bob"));
		assertEquals(FACTORY.wrap("hope"), wonton.get("[bob]"));
		wonton.accept((key, value) -> assertEquals(value, wonton.get(key)));
	}

	private void assertWrongType(final Runnable runner) {
		try {
			runner.run();
			fail("didn't throw bad type");
		} catch(final Wonton.InvalidTypeException e) {
			// pass
		}
	}

	private void assertNoKey(final Runnable runner) {
		try {
			runner.run();
			fail("didn't throw bad key");
		} catch(final Wonton.NoSuchKeyException e) {
			// pass
		}
	}

	@Test
	public void testDeep() {
		final Wonton wonton = FACTORY.wrap(singletonMap("root", asList(singletonMap("bob", "hope"), singletonMap("bob", "newhart"))));
		assertEquals(Wonton.Type.STRUCT, wonton.type());
		assertWrongType(() -> wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertEquals(wonton.value(), wonton.asStruct());
		assertWrongType(() -> wonton.asString());
		assertEquals(FACTORY.wrap(asList(singletonMap("bob", "hope"), singletonMap("bob", "newhart"))), wonton.get("root"));
		assertEquals(FACTORY.wrap(singletonMap("bob", "hope")), wonton.get("root[0]"));
		assertEquals(FACTORY.wrap("hope"), wonton.get("root[0].bob"));
		assertEquals(FACTORY.wrap("newhart"), wonton.get("[root][1][bob]"));
		assertEquals(FACTORY.wrap("hope"), wonton.get("root.0.bob"));
		wonton.accept((key, value) -> assertEquals(value, wonton.get(key)));
	}
}
