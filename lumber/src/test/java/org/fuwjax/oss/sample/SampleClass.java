package org.fuwjax.oss.sample;

public class SampleClass {
	public String doSomething(final String name, final int x) {
		return name + x;
	}

	public String doSomethingCrazy(final int count, final String... names) {
		for (int i = 0; i < count; i++) {
			if (i >= 3) {
				return names[i];
			}
		}
		return names[count];
	}
}
