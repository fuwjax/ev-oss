package org.echovantage.sample;

@SampleAnnotation("example")
public class SampleServiceImpl implements SampleService {
	private final String config;

	public SampleServiceImpl(final String config) {
		this.config = config;
	}

	@Override
	public String doSomething(final String arg) {
		return config + ":" + arg;
	}
}
