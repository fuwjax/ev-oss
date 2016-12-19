package org.fuwjax.oss.test;

import static org.fuwjax.oss.util.function.Functions.function;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fuwjax.oss.util.Types;
import org.junit.Test;

public class TypeConversionTest {
	static class X {
		Object object = "oops";
		Cloneable cloneable = new String[] { "yeah" };
		Serializable serializable = "yep";
		String string = "Hello, World!";
		Object[] objectArray = { "1", "3", "4" };
		String[] stringArray = { "asdf", "qwer" };
		int[] intArray = { 1, 2, 3, 4, 5 };
		long[] longArray = { 0l };
		CharSequence[] charSequenceArray = { "ert", "tre" };
		List<?> listOfAnything = Arrays.asList("a thing");
		List<?>[] arrayOfListOfAnything = new List<?>[] { Arrays.asList("wow") };
		boolean pBoolean = true;
		byte pByte = (byte) 0xFE;
		short pShort = (short) 0xBEEF;
		char pChar = 'x';
		int pInt = 0xDEADBEEF;
		long pLong = 0xFEEF1EF0EF00DF00L;
		float pFloat = 3.14F;
		double pDouble = 9.848857801796D;
		ArrayList<String> arrayListOfString = new ArrayList<>(Arrays.asList("one", "two", "three"));
		ArrayList<? extends String> arrayListOfSubString = new ArrayList<>(Arrays.asList("a", "b", "c"));
		ArrayList<?> arrayListOfAnything = new ArrayList<>(Arrays.asList("aaa", "bbb", "ccc"));
		ArrayList<?>[] arrayOfArrayListOfAnything = new ArrayList<?>[] { new ArrayList<>() };
		AbstractList abstractList = new ArrayList<>(Arrays.asList("red", "fish"));
		AbstractList<String> abstractListOfString = new ArrayList<>(Arrays.asList("clear", "fish"));
		AbstractList<? extends String> abstractListOfSubString = new ArrayList<>(Arrays.asList("black", "fish"));
		List list = Arrays.asList("craziness");
		List<String> listOfString = Arrays.asList("sanity");
		Iterable<String> iterableOfString = Arrays.asList("blue", "fish");
		Iterable<? extends String> iterableOfSubString = Arrays.asList("purple", "fish");
		Iterable<? extends CharSequence> iterableOfSubCharSequence = Arrays.asList("aqua", "fish");
		Iterable<?> iterableOfAnything = Arrays.asList("orange", "fish");
		CharSequence charSequence = "abracadabra";
		Iterable<? super String> iterableOfSuperString = Arrays.asList("not", "fish");
		Iterable<? super CharSequence> iterableOfSuperCharSequence = Arrays.asList("not", "fish");
		Y<String> yOfString = new Y<>("bob");
		Y<? extends String> yOfSubString = new Y<>("alice");

		<V> V v(V v) {
			return v;
		}

		<V> List<V> listOfV(V v) {
			return Arrays.asList(v);
		}

		class Y<T> {
			T t;
			List<T> listOfT;
			List<T>[] arrayOfListOfT;
			List<? extends T> listOfSubT;
			ArrayList<T> arrayListOfT;
			Iterable<T> iterableOfT;
			ArrayList<? extends T> arrayListOfSubT;
			Iterable<? extends T> iterableOfSubT;

			public Y(T value) {
				this.t = value;
				this.listOfT = Arrays.asList(value, value);
				this.arrayOfListOfT = new List[] { Arrays.asList(value), Arrays.asList(value) };
				this.listOfSubT = Arrays.asList(value, value, value);
				this.arrayListOfT = new ArrayList<>(Arrays.asList(value));
				this.iterableOfT = Arrays.asList(value);
				this.arrayListOfSubT = new ArrayList<>(Arrays.asList(value, value));
				this.iterableOfSubT = Arrays.asList(value, value);
			}
		}
	}

	private static Map<String, Type> types = new HashMap<>();
	private static Map<Type, Object> values = new HashMap<>();

	private static void register(Type t, Object o) {
		assertNull(types.put(t.getTypeName(), t));
		values.put(t, o);
		System.out.println("Registered: " + t.getTypeName());
	}

	private static void register(Field f, Object x) {
		if (!f.isSynthetic()) {
			f.setAccessible(true);
			register(f.getGenericType(), function(f::get).apply(x));
		}
	}

	private static void register(Method m, Object x) {
		if (!m.isSynthetic()) {
			m.setAccessible(true);
			register(m.getGenericReturnType(), new Object());
		}
	}

	static {
		X x = new X();
		X.Y<String> y = x.new Y<>("hope");
		Arrays.asList(X.class.getDeclaredFields()).forEach(f -> register(f, x));
		Arrays.asList(X.class.getDeclaredMethods()).forEach(m -> register(m, x));
		Arrays.asList(X.Y.class.getDeclaredFields()).forEach(f -> register(f, y));
	}

	private void assertConvert(boolean mustConvert, String from, String to) {
		assertConvert(mustConvert, types.get(from), types.get(to));
	}

	private void assertConvert(boolean mustConvert, Type from, Type to) {
		assertEquals(mustConvert, Types.isSuper(from, to));
	}

	@Test
	public void testIdentityConversion() {
		// 5.1.1 everything converts to itself
		types.values().forEach(t -> assertConvert(true, t, t));
	}

	@Test
	public void testWideningPrimitiveConversion() {
		// 5.1.2 convert from one numeric to another that may lose precision,
		// but not magnitude or range.
		assertConvert(true, "byte", "short");
		assertConvert(true, "byte", "int");
		assertConvert(true, "byte", "long");
		assertConvert(true, "byte", "float");
		assertConvert(true, "byte", "double");
		assertConvert(true, "char", "int");
		assertConvert(true, "char", "long");
		assertConvert(true, "char", "float");
		assertConvert(true, "char", "double");
		assertConvert(true, "short", "int");
		assertConvert(true, "short", "long");
		assertConvert(true, "short", "float");
		assertConvert(true, "short", "double");
		assertConvert(true, "int", "long");
		assertConvert(true, "int", "float");
		assertConvert(true, "int", "double");
		assertConvert(true, "long", "float");
		assertConvert(true, "long", "double");
		assertConvert(true, "float", "double");
	}

	@Test
	public void testWideningReferenceConversion() {
		assertConvert(true, "java.util.List<V>", "java.util.List<java.lang.String>");
		assertConvert(true, "V", "java.lang.String");

		// 5.1.5 converting from a reference type to a supertype
		// 4.10.2 the supertypes of a parameterized type C<T1, T2> are
		// superclasses of C
		assertConvert(true, "java.util.ArrayList<java.lang.String>", "java.util.AbstractList");
		assertConvert(true, "java.util.ArrayList<? extends java.lang.String>", "java.util.AbstractList");
		assertConvert(true, "java.util.ArrayList<?>", "java.util.AbstractList");
		assertConvert(true, "java.util.ArrayList<? extends T>", "java.util.AbstractList");
		assertConvert(true, "java.util.ArrayList<T>", "java.util.AbstractList");
		assertConvert(true, "java.lang.String", "java.lang.Object");
		// superinterfaces of C
		assertConvert(true, "java.util.ArrayList<java.lang.String>", "java.util.List");
		assertConvert(true, "java.util.ArrayList<? extends java.lang.String>", "java.util.List");
		assertConvert(true, "java.util.ArrayList<?>", "java.util.List");
		assertConvert(true, "java.util.ArrayList<? extends T>", "java.util.List");
		assertConvert(true, "java.util.ArrayList<T>", "java.util.List");
		assertConvert(true, "java.lang.String", "java.lang.CharSequence");
		// Object if C is an interface type with no super interfaces
		assertConvert(true, "java.lang.Iterable<java.lang.String>", "java.lang.Object");
		assertConvert(true, "java.lang.Iterable<? extends java.lang.String>", "java.lang.Object");
		assertConvert(true, "java.lang.Iterable<?>", "java.lang.Object");
		assertConvert(true, "java.lang.Iterable<? extends T>", "java.lang.Object");
		assertConvert(true, "java.lang.Iterable<T>", "java.lang.Object");
		assertConvert(true, "java.lang.CharSequence", "java.lang.Object");
		// The raw type C
		assertConvert(true, "java.util.ArrayList<java.lang.String>", "java.util.AbstractList");
		assertConvert(true, "java.util.ArrayList<? extends java.lang.String>", "java.util.AbstractList");
		assertConvert(true, "java.util.ArrayList<?>", "java.util.AbstractList");
		assertConvert(true, "java.util.ArrayList<? extends T>", "java.util.AbstractList");
		assertConvert(true, "java.util.ArrayList<T>", "java.util.AbstractList");
		// Direct supertype substitution for parameterized types
		assertConvert(true, "java.util.ArrayList<java.lang.String>", "java.util.AbstractList<java.lang.String>");
		assertConvert(true, "java.util.ArrayList<java.lang.String>", "java.util.List<java.lang.String>");
		assertConvert(true, "java.util.ArrayList<? extends java.lang.String>",
				"java.util.AbstractList<? extends java.lang.String>");
		assertConvert(true, "java.util.ArrayList<?>", "java.util.List<?>");
		assertConvert(true, "java.util.ArrayList<? extends T>", "java.lang.Iterable<? extends T>");
		assertConvert(true, "java.util.ArrayList<T>", "java.util.List<T>");
		// Type argument containment for parameterized types (contains 4.5.1). 4
		// cases besides identity
		// extends supertype
		assertConvert(true, "java.lang.Iterable<? extends java.lang.String>",
				"java.lang.Iterable<? extends java.lang.CharSequence>");
		// super subtype
		assertConvert(true, "java.lang.Iterable<? super java.lang.CharSequence>",
				"java.lang.Iterable<? super java.lang.String>");
		// explicit extends
		assertConvert(true, "java.lang.Iterable<java.lang.String>", "java.lang.Iterable<? extends java.lang.String>");
		assertConvert(true, "java.lang.Iterable<java.lang.String>",
				"java.lang.Iterable<? extends java.lang.CharSequence>");
		// explicit super
		assertConvert(true, "java.lang.Iterable<java.lang.String>", "java.lang.Iterable<? super java.lang.String>");
		// 4.10.3 subtypes of parameterized arrays
		// components are subtypes
		assertConvert(true, "java.util.ArrayList<?>[]", "java.util.List<?>[]");
		assertConvert(true, "java.lang.String[]", "java.lang.CharSequence[]");
		assertConvert(true, "java.lang.String[]", "java.lang.Object[]");
		// Object[] is an Object, Cloneable, and Serializable
		assertConvert(true, "java.lang.Object[]", "java.lang.Object");
		assertConvert(true, "java.lang.String[]", "java.lang.Object");
		assertConvert(true, "java.lang.Object[]", "java.lang.Cloneable");
		assertConvert(true, "java.lang.String[]", "java.lang.Cloneable");
		assertConvert(true, "java.lang.Object[]", "java.io.Serializable");
		assertConvert(true, "java.lang.String[]", "java.io.Serializable");
		// so are primitive arrays
		assertConvert(true, "int[]", "java.lang.Object");
		assertConvert(true, "int[]", "java.lang.Cloneable");
		assertConvert(true, "int[]", "java.io.Serializable");
		assertConvert(false, "int[]", "long[]");
		assertConvert(true,
				"org.fuwjax.oss.test.TypeConversionTest$X.org.fuwjax.oss.test.TypeConversionTest$X$Y<java.lang.String>",
				"org.fuwjax.oss.test.TypeConversionTest$X.org.fuwjax.oss.test.TypeConversionTest$X$Y<? extends java.lang.String>");
	}
}
