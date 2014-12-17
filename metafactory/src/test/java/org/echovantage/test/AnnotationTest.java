package org.echovantage.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ServiceLoader;

import org.echovantage.compile.RuntimeClassLoader;
import org.echovantage.sample.SampleAnnotation;
import org.echovantage.sample.SampleService;
import org.junit.Test;

public class AnnotationTest {
	@Test
	public void testMeta() throws IOException {
		final RuntimeClassLoader loader = new RuntimeClassLoader(System.out);
		assertTrue(loader.compile(Paths.get("src/test/java")));
		final ServiceLoader<SampleService.Factory> factories = ServiceLoader.load(SampleService.Factory.class, loader);
		int countMetaFactory = 0;
		int countMetaService = 0;
		for(final SampleService.Factory factory : factories) {
			if(factory.getClass().isAnnotationPresent(SampleAnnotation.class)) {
				assertEquals("factory", factory.getClass().getAnnotation(SampleAnnotation.class).value());
				countMetaFactory++;
			} else {
				countMetaService++;
			}
			assertEquals("config:something", factory.create("config").doSomething("something"));
		}
		assertEquals(countMetaFactory, 1);
		assertEquals(countMetaService, 2);
	}
}
