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
package org.echovantage.gild.stage;

import static org.echovantage.gild.stage.StandardStageFactory.FileType.FINAL_OUTPUT;
import static org.echovantage.gild.stage.StandardStageFactory.FileType.RAW_OUTPUT;
import static org.echovantage.gild.stage.StandardStageFactory.FileType.TEST_DATA;
import static org.echovantage.gild.stage.StandardStageFactory.StageState.PREPARE;
import static org.echovantage.gild.stage.StandardStageFactory.StageState.PRESERVE;
import static org.echovantage.util.Files2.delete;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.runners.model.FrameworkMethod;

public class StandardStageFactory implements StageFactory {
	public enum StageState {
		PREPARE, PRESERVE
	}

	public enum FileType {
		TEST_DATA, RAW_OUTPUT, FINAL_OUTPUT
	}

	@Override
	public final Stage stage(final FrameworkMethod method) throws IOException {
		for(final Path working : workingPaths(method)) {
			delete(working);
		}
		return new Stage() {
			@Override
			public Path comparePath(final String service) {
				return path(FINAL_OUTPUT, method, PRESERVE, service);
			}

			@Override
			public Path goldPath(final String service) {
				return path(TEST_DATA, method, PRESERVE, service);
			}

			@Override
			public Path inputPath(final String service) {
				return path(TEST_DATA, method, PREPARE, service);
			}

			@Override
			public Path transformPath(final String service) {
				return path(RAW_OUTPUT, method, PRESERVE, service);
			}
		};
	}

	protected List<Path> workingPaths(final FrameworkMethod method) {
		return Arrays.asList(path(FileType.FINAL_OUTPUT, method), path(FileType.RAW_OUTPUT, method));
	}

	protected Path path(final FileType type, final FrameworkMethod method) {
		return Paths.get(segment(type), method.getDeclaringClass().getName(), method.getName());
	}

	protected String segment(final FileType type) {
		switch(type) {
			case TEST_DATA:
				return "src/test/gild";
			case RAW_OUTPUT:
				return "target/gild/raw";
			case FINAL_OUTPUT:
				return "target/test/gild";
			default:
				throw new UnsupportedOperationException(type.toString());
		}
	}

	protected Path path(final FileType type, final FrameworkMethod method, final StageState state, final String service) {
		return path(type, method).resolve(segment(state)).resolve(service);
	}

	protected String segment(final StageState state) {
		switch(state) {
			case PREPARE:
				return "input";
			case PRESERVE:
				return "output";
			default:
				throw new UnsupportedOperationException(state.toString());
		}
	}
}
