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
package org.fuwjax.oss.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.fuwjax.oss.util.Log;

public class ProxyConfig {
	private Function<String, String> STRING = String::valueOf;
	private Function<String, Integer> INT = Integer::valueOf;
	private Function<String, Long> LONG = Long::valueOf;

	private final Map<String, String> parameters = new HashMap<>();
	private Executor executor;
	private Log log;

	public ProxyConfig(Log log, Map<String, String> parameters) {
		this(log, null, parameters);
	}

	public ProxyConfig(Log log, Executor executor, Map<String, String> parameters) {
		this.log = log;
		this.executor = executor;
		this.parameters.putAll(parameters);
	}

	private <T> T param(String key, Function<String, T> cast, T defaultValue) {
		return parameters.containsKey(key) ? cast.apply(parameters.get(key)) : defaultValue;
	}

	public Log getLog() {
		return log;
	}

	public long timeout(long defaultValue) {
		return param("timeout", LONG, defaultValue);
	}

	public String hostHeader() {
		return param("hostHeader", STRING, null);
	}

	public String viaHost(String defaultValue) {
		return param("viaHost", STRING, defaultValue);
	}

	public HttpClient createClient() {
		HttpClient client = new HttpClient();

		int maxThreads = param("maxThreads", INT, 0);
		if (executor != null){
			client.setExecutor(executor);
		}else if (maxThreads > 0) {
			QueuedThreadPool qtp = new QueuedThreadPool(maxThreads);
			qtp.setName(param("name", STRING, "Proxy"));
			client.setExecutor(qtp);
		}

		client.setMaxConnectionsPerDestination(param("maxConnections", INT, 256));
		client.setIdleTimeout(param("idleTimeout", LONG, 30000L));
		client.setRequestBufferSize(param("requestBufferSize", INT, client.getRequestBufferSize()));
		client.setResponseBufferSize(param("responseBufferSize", INT, client.getResponseBufferSize()));
		return client;
	}

}
