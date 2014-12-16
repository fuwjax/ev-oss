package org.echovantage.test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.echovantage.sample.SampleMapObject;
import org.echovantage.wonton.Wonton;
import org.junit.Test;

public class WontonTest {
	@Test
	public void testNull() {
		final Wonton wonton = Wonton.wontonOf(null);
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
		final Wonton wonton = Wonton.wontonOf(true);
		assertEquals(Wonton.Type.BOOLEAN, wonton.type());
		assertTrue((Boolean) wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertEquals(true, wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testFalse() {
		final Wonton wonton = Wonton.wontonOf(false);
		assertEquals(Wonton.Type.BOOLEAN, wonton.type());
		assertFalse((Boolean) wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertEquals(false, wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testDouble() {
		final Wonton wonton = Wonton.wontonOf(5.3);
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
		final Wonton wonton = Wonton.wontonOf(5);
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
		final Wonton wonton = Wonton.wontonOf("Hello, World!");
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
		final Wonton wonton = Wonton.wontonOf(asList(1, 2, 3));
		assertEquals(Wonton.Type.ARRAY, wonton.type());
		assertEquals(asList(Wonton.wontonOf(1), Wonton.wontonOf(2), Wonton.wontonOf(3)), wonton.value());
		assertEquals(wonton.value(), wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
		assertEquals(Wonton.wontonOf(1), wonton.get(Wonton.path("[0]")));
		assertEquals(Wonton.wontonOf(1), wonton.get(Wonton.pathOf("0")));
		assertNoKey(() -> wonton.get(Wonton.pathOf("12")));
		assertNoKey(() -> wonton.get(Wonton.path("[9]")));
		assertNoKey(() -> wonton.get(Wonton.pathOf("bob")));
		wonton.accept((key, value) -> assertEquals(value, wonton.get(key)));
	}

	@Test
	public void testPrimitiveArray() {
		final Wonton wonton = Wonton.wontonOf(new int[] { 1, 2, 3 });
		assertEquals(Wonton.Type.ARRAY, wonton.type());
		assertEquals(asList(Wonton.wontonOf(1), Wonton.wontonOf(2), Wonton.wontonOf(3)), wonton.value());
		assertEquals(wonton.value(), wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testObjectArray() {
		final Wonton wonton = Wonton.wontonOf(new Object[] { 1, 2, 3 });
		assertEquals(Wonton.Type.ARRAY, wonton.type());
		assertEquals(asList(Wonton.wontonOf(1), Wonton.wontonOf(2), Wonton.wontonOf(3)), wonton.value());
		assertEquals(wonton.value(), wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertWrongType(() -> wonton.asStruct());
		assertWrongType(() -> wonton.asString());
	}

	@Test
	public void testMap() {
		final Wonton wonton = Wonton.wontonOf(singletonMap("bob", "hope"));
		assertEquals(Wonton.Type.STRUCT, wonton.type());
		assertEquals(singletonMap("bob", Wonton.wontonOf("hope")), wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertEquals(wonton.value(), wonton.asStruct());
		assertWrongType(() -> wonton.asString());
		assertEquals(Wonton.wontonOf("hope"), wonton.get(Wonton.path("bob")));
		assertEquals(Wonton.wontonOf("hope"), wonton.get(Wonton.path("[bob]")));
		wonton.accept((key, value) -> assertEquals(value, wonton.get(key)));
	}

	@Test
	public void testObject() {
		final Wonton wonton = Wonton.wontonOf(new SampleMapObject(5, "bob", "hope"));
		assertEquals(Wonton.Type.STRUCT, wonton.type());
		final Map<String, Object> map = new HashMap<>();
		map.put("id", 5);
		map.put("name", "bob");
		map.put("description", "hope");
		assertEquals(Wonton.wontonOf(map).value(), wonton.value());
		assertWrongType(() -> wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertEquals(wonton.value(), wonton.asStruct());
		assertWrongType(() -> wonton.asString());
		assertEquals(Wonton.wontonOf("hope"), wonton.get(Wonton.path("description")));
		assertEquals(Wonton.wontonOf("bob"), wonton.get(Wonton.path("[name]")));
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
		} catch(final Wonton.NoSuchPathException e) {
			// pass
		}
	}

	@Test
	public void testDeep() {
		final Wonton wonton = Wonton.wontonOf(singletonMap("root", asList(singletonMap("bob", "hope"), singletonMap("bob", "newhart"))));
		assertEquals(Wonton.Type.STRUCT, wonton.type());
		assertWrongType(() -> wonton.asArray());
		assertWrongType(() -> wonton.asBoolean());
		assertWrongType(() -> wonton.asNumber());
		assertEquals(wonton.value(), wonton.asStruct());
		assertWrongType(() -> wonton.asString());
		assertEquals(Wonton.wontonOf(asList(singletonMap("bob", "hope"), singletonMap("bob", "newhart"))), wonton.get(Wonton.path("root")));
		assertEquals(Wonton.wontonOf(singletonMap("bob", "hope")), wonton.get(Wonton.path("root[0]")));
		assertEquals(Wonton.wontonOf("hope"), wonton.get(Wonton.path("root[0].bob")));
		assertEquals(Wonton.wontonOf("newhart"), wonton.get(Wonton.path("[root][1][bob]")));
		assertEquals(Wonton.wontonOf("newhart"), wonton.get(Wonton.pathOf("root", "1", "bob")));
		assertEquals(Wonton.wontonOf("hope"), wonton.get(Wonton.path("root.0.bob")));
		wonton.accept((key, value) -> assertEquals(value, wonton.get(key)));
	}
}
