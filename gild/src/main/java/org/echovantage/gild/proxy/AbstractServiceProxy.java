/**
 * Copyright (C) 2014 EchoVantage (info@echovantage.com)
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
package org.echovantage.gild.proxy;

import static org.echovantage.util.Assert2.assertCompletes;
import static org.echovantage.util.Assert2.assertReturns;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;

import org.echovantage.util.ReadOnlyPath;

public abstract class AbstractServiceProxy implements ServiceProxy {
	private ReadOnlyPath bufferedInput;
	private Path bufferedOutput;
	private boolean configured;

	protected AbstractServiceProxy() {
		// must call configured
	}

	protected AbstractServiceProxy(final boolean configured) {
		this.configured = configured;
	}

	@Override
	public final void prepare(final ReadOnlyPath input, final Path output) {
		if(configured) {
			assertCompletes(() -> prepareImpl(input, output));
		} else {
			bufferedInput = input;
			bufferedOutput = output;
		}
	}

	@Override
	public final void preserve(final Path output, final ReadOnlyPath golden) {
		assertTrue("Proxy is not configured", configured);
		configured = assertReturns(() -> preserveImpl(output, golden));
	}

	protected abstract boolean preserveImpl(final Path output, final ReadOnlyPath golden) throws Exception;

	protected void assertNotConfigured() {
		assertFalse("Proxy is already configured", configured);
	}

	protected void configured() {
		if(!configured) {
			configured = true;
			if(bufferedInput != null) {
				final ReadOnlyPath input = bufferedInput;
				bufferedInput = null;
				final Path output = bufferedOutput;
				bufferedOutput = null;
				assertCompletes(() -> prepareImpl(input, output));
			}
		}
	}

	protected abstract void prepareImpl(ReadOnlyPath input, Path output) throws Exception;
}
