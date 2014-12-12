package org.echovantage.sample;

import org.echovantage.metafactory.MetaService;

@MetaService
public class SampleServiceImpl implements SampleService {
	@Override
	public String doSomething(final String arg) {
		return "service:" + arg;
	}
}
