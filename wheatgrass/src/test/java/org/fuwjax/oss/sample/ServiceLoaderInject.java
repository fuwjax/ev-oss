package org.fuwjax.oss.sample;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public class ServiceLoaderInject {
	public ServiceLoaderInject(ServiceLoader<Supplier<?>> suppliers){
		suppliers.reload();
	}
}
