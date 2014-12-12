package org.echovantage.sample;

@SampleAnnotation("factory")
public class SampleServiceWithFactoryImpl implements SampleService {
	private final String config;

	public SampleServiceWithFactoryImpl(final String config) {
		this.config = config;
	}

	@Override
	public String doSomething(final String arg) {
		return config + ":" + arg;
	}
}
