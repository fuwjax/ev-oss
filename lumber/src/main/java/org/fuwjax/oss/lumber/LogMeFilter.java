package org.fuwjax.oss.lumber;

import java.util.List;

import javassist.CtMethod;

public class LogMeFilter implements LumberFilter {
	private List<String> classes;

	@Override
	public void configure(final Object configData) throws Exception {
		classes = (List<String>) configData;
	}

	@Override
	public boolean mightTransform(final String className) {
		return classes.contains(className);
	}

	@Override
	public boolean shouldTransform(final CtMethod method) {
		return method.hasAnnotation(LogMe.class);
	}

}
