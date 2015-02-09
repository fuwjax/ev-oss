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
package org.echovantage.sample;

import org.echovantage.metafactory.MetaService;

public class SampleServiceInnerFactoryImpl implements SampleService {
	private final String config;

	@MetaService
	public static class SampleServiceFactory implements SampleService.Factory {
		@Override
		public SampleService create(final String config) {
			return new SampleServiceInnerFactoryImpl(config);
		}
	}

	public SampleServiceInnerFactoryImpl(final String config) {
		this.config = config;
	}

	@Override
	public String doSomething(final String arg) {
		return config + ":" + arg;
	}
}
