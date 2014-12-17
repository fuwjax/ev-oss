package org.echovantage.sample;

import org.echovantage.metafactory.MetaService;

@MetaService
public class SampleServiceFactoryImpl implements SampleService.Factory {
	@Override
	public SampleService create(final String config) {
		return arg -> config + ":" + arg;
	}
}
