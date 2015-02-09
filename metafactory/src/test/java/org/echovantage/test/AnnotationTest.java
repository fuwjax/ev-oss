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
