package org.fuwjax.oss.sample.module;

import java.util.ServiceLoader;

public class ServiceLoaderModule {
	public <T> ServiceLoader<T> services(Class<T> cls){
		return ServiceLoader.load(cls);
	}
}
