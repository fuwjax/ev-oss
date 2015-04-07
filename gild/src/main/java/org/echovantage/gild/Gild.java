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
package org.echovantage.gild;

import static org.echovantage.util.Assert2.assertEquals;
import static org.echovantage.util.Files2.delete;
import static org.echovantage.util.ReadOnlyPath.readOnly;
import static org.echovantage.util.function.Functions.supplier;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.echovantage.gild.proxy.ServiceProxy;
import org.echovantage.gild.stage.Stage;
import org.echovantage.gild.stage.StageFactory;
import org.echovantage.gild.stage.StandardStageFactory;
import org.echovantage.gild.transform.Transformer;
import org.echovantage.inject.BindConstraint;
import org.echovantage.inject.Injector;
import org.echovantage.inject.Optional;
import org.echovantage.inject.Scope;
import org.echovantage.util.Arrays2;
import org.echovantage.util.Files2;
import org.echovantage.util.RunWrapException;
import org.echovantage.util.function.Functions;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * The Gilded Test Harness jUnit Rule. The Gilded harness does a gold copy
 * restore and compare through various services generally external to the system
 * under test.
 * <p>
 * Standard Usage:
 *
 * <pre>
 * package org.example.test;
 * 
 * public class TestClass {
 * 	private DatabaseProxy db = new DatabaseProxy(dataSource);
 * 	\@Rule
 * 	public Gilded harness = new Gilded().with("db", db);
 * 
 * 	\@Test
 * 	public void testSomething() throws Exception {
 * 		// loads src/test/gilded/org.example.test.TestClass/testSomething/input/db/* into the dataSource
 * 		SystemUnderTest sys = new SystemUnderTest(dataSource);
 * 		sys.doSomething();
 * 		// saves the dataSource to target/test/gilded/org.example.test.TestClass/testSomething/output/db/*
 * 		// asserts that the target/.../output/db/* is byte-equivalent to src/.../output/db/*
 * 	}
 * }
 * </pre>
 *
 * The harness can create the gold copy instead of asserting against it.
 *
 * <pre>
 * 	\@Rule Gilded harness = new Gilded().with("db", db).updateGoldCopy();
 * </pre>
 *
 * By default, the harness uses the standard path locations detailed in
 * {@link StandardStageFactory} and has no {@link Transformer}.
 * @author fuwjax
 */
public class Gild implements MethodRule {
	private boolean isAssert = true;
	private boolean prepared;
	private final Scope injector;
	@Inject
	@Optional
	private Transformer transform;
	@Inject
	private StageFactory stages;

	public Gild(final Object... modules) {
		try {
			injector = Injector.newInjector(Arrays2.append(modules, supplier(StandardStageFactory::new))).scope();
			injector.inject(this);
		} catch(final ReflectiveOperationException e) {
			throw new RunWrapException(e);
		}
	}

	/**
	 * Turns this test execution into a gold copy create instead of a gold copy
	 * assert. This method may have vairous safeguards to prevent it from being
	 * accidentally left in code during a release.
	 * @return this harness
	 */
	public Gild updateGoldCopy() {
		isAssert = false;
		return this;
	}

	@Override
	public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				execute(base, method, target);
			}
		};
	}

	protected void execute(final Statement statement, final FrameworkMethod method, final Object target) throws Throwable {
		injector.inject(target);
		final Stage stage = stages.stage(method);
		final Map<BindConstraint, ServiceProxy> proxies = injector.bindings(ServiceProxy.class);
		prepare(stage, proxies);
		statement.evaluate();
		preserve(stage, proxies);
	}

	private void prepare(final Stage stage, final Map<BindConstraint, ServiceProxy> proxies) {
		assertFalse("Stage has already been prepared", prepared);
		for(final Map.Entry<BindConstraint, ServiceProxy> entry : proxies.entrySet()) {
			final String service = entry.getKey().;
			final ServiceProxy proxy = entry.getValue();
			final Path in = stage.inputPath(service);
			final Path output = transform == null ? stage.comparePath(service) : stage.transformPath(service);
			proxy.prepare(readOnly(in), output);
		}
		prepared = true;
	}

	private void preserve(final Stage stage, final Map<BindConstraint, ServiceProxy> proxies) throws Exception {
		if(!prepared) {
			return;
		}
		prepared = false;
		for(final Map.Entry<BindConstraint, ServiceProxy> entry : proxies.entrySet()) {
			final String service = entry.getKey();
			final Path gold = stage.goldPath(service);
			final Path compare = stage.comparePath(service);
			if(transform == null) {
				entry.getValue().preserve(compare, readOnly(gold));
			} else {
				final Path output = stage.transformPath(service);
				entry.getValue().preserve(output, readOnly(gold));
				transform.transform(readOnly(output), compare);
			}
			assertGolden(gold, compare);
		}
	}

	private void assertGolden(final Path expected, final Path actual) throws IOException {
		if(isAssert) {
			assertEquals(expected, actual);
		} else {
			delete(expected);
			Files2.copy(actual, expected);
			fail("Gold copy has been updated");
		}
	}
}
