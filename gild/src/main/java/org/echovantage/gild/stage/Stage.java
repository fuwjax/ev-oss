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

import java.nio.file.Path;

import org.echovantage.gild.proxy.ServiceProxy;

/**
 * Manages the paths used by a stage during a Glid test execution. It's
 * important that none of these methods return null. They may throw an exception
 * if they truly have no analog in the file system. They should be returned
 * whether they exist, it's simply where in the file system they would occur,
 * regardless of whether they actually do.
 * @author fuwjax
 */
public interface Stage {
	/**
	 * The input path for the service.
	 * @param service the service name of one of the registered
	 *           {@link ServiceProxy} instances
	 * @return the input path, never null
	 */
	Path inputPath(String service);

	/**
	 * The gold copy path for the service.
	 * @param service the service name of one of the registered
	 *           {@link ServiceProxy} instances
	 * @return the gold copy path, never null
	 */
	Path goldPath(String service);

	/**
	 * The compare copy (transformed output) path for the service.
	 * @param service the service name of one of the registered
	 *           {@link ServiceProxy} instances
	 * @return the compare copy path, never null
	 */
	Path comparePath(String service);

	/**
	 * The pre-transform (raw output) path for the service.
	 * @param service the service name of one of the registered
	 *           {@link ServiceProxy} instances
	 * @return the pre-transform path, never null
	 */
	Path transformPath(String service);
}
