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
package org.echovantage.gild.proxy;

import static org.echovantage.util.assertion.Assert2.assertCompletes;

import java.nio.file.Files;
import java.nio.file.Path;

import org.echovantage.util.io.Files2;
import org.echovantage.util.io.ReadOnlyPath;

public class FileSystemProxy extends AbstractServiceProxy {
	private Path working;

	public FileSystemProxy() {
		// must call setWorkingDirectory
	}

	public FileSystemProxy(final Path working) {
		setWorkingDirectory(working);
	}

	@Override
	protected void prepareImpl(final ReadOnlyPath input, final Path output) throws Exception {
		Files2.delete(working);
		Files.createDirectories(working);
		input.copyTo(working);
	}

	@Override
	protected boolean preserveImpl(final Path output, final ReadOnlyPath golden) throws Exception {
		Files.createDirectories(output);
		Files2.copy(working, output);
		return true;
	}

	public void setWorkingDirectory(final Path working) {
		this.working = working;
		assertCompletes(this::configured);
	}
}
