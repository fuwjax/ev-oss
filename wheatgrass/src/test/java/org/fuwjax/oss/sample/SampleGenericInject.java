package org.fuwjax.oss.sample;

import javax.inject.Inject;

import org.fuwjax.oss.util.ValueObject;

public class SampleGenericInject<T> extends ValueObject{
	@Inject
	private T value;
	
	public SampleGenericInject() {
		deferId(this::getClass, this::value);
	}
	
	public T value(){
		return value;
	}
}
