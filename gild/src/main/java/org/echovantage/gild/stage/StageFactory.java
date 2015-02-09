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

import java.io.IOException;

import org.junit.runner.Description;

/**
 * Creates new Stages for the Gild engine.
 *
 * @author fuwjax
 */
public interface StageFactory {
	/**
	 * Creates a new stage for the test. It is the responsibility of the
	 * implementation to delete any paths that will be
	 * returned by {@link Stage#comparePath(String)} or
	 * {@link Stage#transformPath(String)}.
	 *
	 * @param desc
	 *           the jUnit test descriptor
	 * @return the start stage for the test
	 * @throws IOException
	 *            if the working paths cannot be deleted
	 */
	Stage start(final Description desc) throws IOException;
}
