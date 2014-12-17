package org.echovantage.sample;

import org.echovantage.metafactory.MetaService;

public class SampleServiceInnerFactoryImpl implements SampleService {
	private final String config;

	@MetaService
	public static class SampleServiceFactory implements SampleService.Factory {
		@Override
		public SampleService create(final String config) {
			return new SampleServiceInnerFactoryImpl(config);
		}
	}

	public SampleServiceInnerFactoryImpl(final String config) {
		this.config = config;
	}

	@Override
	public String doSomething(final String arg) {
		return config + ":" + arg;
	}
}
