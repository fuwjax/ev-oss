package org.echovantage.sample;

public interface SampleService {
	interface Factory {
		SampleService create(String config);
	}

	String doSomething(String arg);
}
