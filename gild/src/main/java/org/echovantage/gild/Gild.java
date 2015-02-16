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

import static org.echovantage.util.Assert2.assertCompletes;
import static org.echovantage.util.Assert2.assertEquals;
import static org.echovantage.util.Files2.delete;
import static org.echovantage.util.ReadOnlyPath.readOnly;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.echovantage.gild.proxy.ServiceProxy;
import org.echovantage.gild.stage.Stage;
import org.echovantage.gild.stage.StageFactory;
import org.echovantage.gild.stage.StandardStageFactory;
import org.echovantage.gild.transform.Transformer;
import org.echovantage.util.Files2;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
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
 * <p>
 * Additionally, the harness supports staged execution. For example:
 *
 * <pre>
 * 	\@Rule
 * 	public Gilded harness = new Gilded().with("db", db).staged(StandardStageFactory.startingAt("stage1"));
 * 
 * 	\@Test
 * 	public void testProcess() throws Exception {
 * 		// loads src/test/gilded/org.example.test.TestClass/testProcess/stage1/input/db/* into the dataSource
 * 		SystemUnderTest sys = new SystemUnderTest(dataSource);
 * 		sys.doSomething();
 * 		harness.nextStage("stage2");
 * 		// saves the dataSource to target/test/gilded/org.example.test.TestClass/testProcess/stage1/output/db/*
 * 		// asserts that the target/.../stage1/output/db/* is byte-equivalent to src/.../stage1/output/db/*
 * 		// loads src/test/gilded/org.example.test.TestClass/testProcess/stage2/input/db/* into the dataSource
 * 		sys.doSomethingElse();
 * 		// saves the dataSource to target/test/gilded/org.example.test.TestClass/testProcess/stage2/output/db/*
 * 		// asserts that the target/.../stage2/output/db/* is byte-equivalent to src/.../stage2/output/db/*
 * 	}
 * </pre>
 * <p>
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
public class Gild implements TestRule {
	private Stage stage;
	private StageFactory stages = new StandardStageFactory(null);
	private final Map<String, ServiceProxy> proxies = new LinkedHashMap<>();
	private Transformer transform;
	private boolean isAssert = true;
	private boolean prepared;

	/**
	 * Adds a service proxy to this harness.
	 * @param serviceName the name of the service
	 * @param proxy the service proxy
	 * @return this harness
	 */
	public Gild with(final String serviceName, final ServiceProxy proxy) {
		proxies.put(serviceName, proxy);
		return this;
	}

	public Gild staged(final StageFactory stages) {
		this.stages = stages;
		return this;
	}

	public Gild transformedBy(final Transformer transformer) {
		transform = transformer;
		return this;
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
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				execute(base, description);
			}
		};
	}

	protected void execute(final Statement statement, final Description description) throws Throwable {
		stage = stages.start(description);
		prepare();
		statement.evaluate();
		preserve();
	}

	private void prepare() {
		assertFalse("Stage has already been prepared", prepared);
		for(final Map.Entry<String, ServiceProxy> entry : proxies.entrySet()) {
			final String service = entry.getKey();
			final ServiceProxy proxy = entry.getValue();
			final Path in = stage.inputPath(service);
			final Path output = transform == null ? stage.comparePath(service) : stage.transformPath(service);
			proxy.prepare(readOnly(in), output);
		}
		prepared = true;
	}

	public void assertGolden() {
		assertCompletes(() -> preserve());
	}

	private void preserve() throws Exception {
		if(!prepared) {
			return;
		}
		prepared = false;
		for(final Map.Entry<String, ServiceProxy> entry : proxies.entrySet()) {
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

	/**
	 * Moves to the next stage in the staged test run.
	 * @param stageName the next stage name
	 */
	public void nextStage(final String stageName) {
		assertNotNull("Cannot move to a null stage", stageName);
		assertGolden();
		stage = stage.nextStage(stageName);
		assertCompletes(() -> prepare());
	}
}
