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

import org.eclipse.jetty.util.thread.QueuedThreadPool;

import org.fuwjax.oss.util.Log;

public class ProxyConfig {
	private final String name;
	private final Map<String, String> parameters = new HashMap<>();
	private Executor executor;
	private Log log;
	
	public ProxyConfig(String name, Log log, Executor executor, Map<String, String> parameters){
		this.name = name;
		this.log = log;
		this.executor = executor;
		this.parameters.putAll(parameters);
	}

	public String getInitParameter(String key) {
		return parameters.get(key);
	}

	public Executor getExecutor() {
        String value = getInitParameter("maxThreads");
        if (value == null || "-".equals(value))
        {
            if (executor==null)
                throw new IllegalStateException("No server executor for proxy");
            return executor;
        }
        else
        {
            QueuedThreadPool qtp= new QueuedThreadPool(Integer.parseInt(value));
            qtp.setName(name);
            return qtp;
        }
	}

	public Log getLog() {
		return log;
	}
}
