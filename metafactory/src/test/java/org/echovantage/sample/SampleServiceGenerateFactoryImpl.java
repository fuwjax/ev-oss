package org.echovantage.sample;

@SampleAnnotation("factory")
public class SampleServiceGenerateFactoryImpl implements SampleService {
	private final String config;

	public SampleServiceGenerateFactoryImpl(final String config) {
		this.config = config;
	}

	@Override
	public String doSomething(final String arg) {
		return config + ":" + arg;
	}
}
