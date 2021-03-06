/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
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
package org.fuwjax.oss.gild.stage;

import static org.fuwjax.oss.gild.stage.StandardStageFactory.FileType.FINAL_OUTPUT;
import static org.fuwjax.oss.gild.stage.StandardStageFactory.FileType.RAW_OUTPUT;
import static org.fuwjax.oss.gild.stage.StandardStageFactory.FileType.TEST_DATA;
import static org.fuwjax.oss.gild.stage.StandardStageFactory.StageState.PREPARE;
import static org.fuwjax.oss.gild.stage.StandardStageFactory.StageState.PRESERVE;
import static org.fuwjax.oss.util.io.Files2.delete;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.fuwjax.oss.util.Paths2;
import org.fuwjax.oss.util.RunWrapException;
import org.junit.runner.Description;

public class StandardStageFactory implements StageFactory {
	public static final StageFactory CLASSPATH_STAGING = new StandardStageFactory(null) {
		@Override
		protected Path path(final FileType type, final Description desc) {
			if (type == FileType.TEST_DATA) {
				try {
					return Paths2.classpath("gild", desc.getClassName(), desc.getMethodName());
				} catch (final Exception e) {
					throw new RunWrapException(e);
				}
			}
			return super.path(type, desc);
		}
	};

	public enum StageState {
		PREPARE, PRESERVE
	}

	public enum FileType {
		TEST_DATA, RAW_OUTPUT, FINAL_OUTPUT
	}

	public static StandardStageFactory startingAt(final String startStage) {
		return new StandardStageFactory(startStage);
	}

	private final String startStage;

	public StandardStageFactory(final String startStage) {
		this.startStage = startStage;
	}

	@Override
	public final Stage start(final Description desc) throws IOException {
		for (final Path working : workingPaths(desc)) {
			delete(working);
		}
		return stage(desc, startStage);
	}

	protected List<Path> workingPaths(final Description desc) {
		return Arrays.asList(path(FileType.FINAL_OUTPUT, desc), path(FileType.RAW_OUTPUT, desc));
	}

	private Stage stage(final Description desc, final String stage) {
		return new Stage() {
			@Override
			public Path comparePath(final String service) {
				return path(FINAL_OUTPUT, desc, stage, PRESERVE, service);
			}

			@Override
			public Path goldPath(final String service) {
				return path(TEST_DATA, desc, stage, PRESERVE, service);
			}

			@Override
			public Path inputPath(final String service) {
				return path(TEST_DATA, desc, stage, PREPARE, service);
			}

			@Override
			public Stage nextStage(final String stageName) {
				return stage(desc, stageName);
			}

			@Override
			public Path transformPath(final String service) {
				return path(RAW_OUTPUT, desc, stage, PRESERVE, service);
			}
		};
	}

	protected final String startStage() {
		return startStage;
	}

	protected Path path(final FileType type, final Description desc) {
		return Paths.get(segment(type), desc.getClassName(), desc.getMethodName());
	}

	protected String segment(final FileType type) {
		switch (type) {
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

	protected Path path(final FileType type, final Description desc, final String stage, final StageState state, final String service) {
		Path path = path(type, desc);
		if (stage != null) {
			path = path.resolve(stage);
		}
		return path.resolve(segment(state)).resolve(service);
	}

	protected String segment(final StageState state) {
		switch (state) {
		case PREPARE:
			return "input";
		case PRESERVE:
			return "output";
		default:
			throw new UnsupportedOperationException(state.toString());
		}
	}
}
