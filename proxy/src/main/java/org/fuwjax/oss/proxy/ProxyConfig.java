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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.fuwjax.oss.util.Log;

public class ProxyConfig {
	private Function<String, String> STRING = String::valueOf;
	private Function<String, Integer> INT = Integer::valueOf;
	private Function<String, Long> LONG = Long::valueOf;
	private Function<String, Boolean> BOOLEAN = Boolean::valueOf;
	private Function<String, Set<String>> SET =  ProxyConfig::parseList;
	
    private static Set<String> parseList(String list)
    {
        Set<String> result = new HashSet<>();
        String[] hosts = list.split(",");
        for (String host : hosts)
        {
            host = host.trim();
            if (host.length() == 0)
                continue;
            result.add(host);
        }
        return result;
    }

	private final Map<String, String> parameters = new HashMap<>();
	private Executor executor;
	private Log log;
	
	public ProxyConfig(Log log, Map<String, String> parameters){
		this(log, null, parameters);
	}
	
	public ProxyConfig(Log log, Executor executor, Map<String, String> parameters){
		this.log = log;
		this.executor = executor;
		this.parameters.putAll(parameters);
	}

	private <T> T param(String key, Function<String, T> cast, T defaultValue){
		return parameters.containsKey(key) ? cast.apply(parameters.get(key)) : defaultValue;
	}

	public Executor getExecutor() {
		int maxThreads = maxThreads(0);
        if (maxThreads == 0)
        {
            if (executor==null)
                throw new IllegalStateException("No server executor for proxy");
            return executor;
        }
        else
        {
            QueuedThreadPool qtp= new QueuedThreadPool(maxThreads);
            qtp.setName(name());
            return qtp;
        }
	}

	public Log getLog() {
		return log;
	}
	
	public String name(){
		return param("name", STRING, "Proxy");
	}
	
	public int maxThreads(int defaultValue) {
		return param("maxThreads", INT, defaultValue);
	}

	public int maxConnections(int defaultValue) {
		return param("maxConnections", INT, defaultValue);
	}

	public long idleTimeout(long defaultValue) {
		return param("idleTimeout", LONG, defaultValue);
	}

	public long timeout(long defaultValue) {
		return param("timeout", LONG, defaultValue);
	}

	public int requestBufferSize(int defaultValue) {
		return param("requestBufferSize", INT, defaultValue);
	}

	public int responseBufferSize(int defaultValue) {
		return param("responseBufferSize", INT, defaultValue);
	}

	public boolean preserveHost() {
		return param("preserveHost", BOOLEAN, false);
	}

	public String hostHeader() {
		return param("hostHeader", STRING, null);
	}

	public String viaHost() {
		return param("viaHost", STRING, null);
	}
	
	public Collection<? extends String> whiteList() {
		return param("whiteList", SET, Collections.emptySet());
	}

	public Collection<? extends String> blackList() {
		return param("blackList", SET, Collections.emptySet());
	}

	public String proxyTo() {
		return param("proxyTo", STRING, null);
	}

	public String prefix(String defaultValue) {
		return param("prefix", STRING, defaultValue);
	}


}
