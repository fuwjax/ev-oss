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
