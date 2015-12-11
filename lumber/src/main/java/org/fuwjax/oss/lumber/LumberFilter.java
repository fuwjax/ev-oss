package org.fuwjax.oss.lumber;

import java.util.function.Predicate;

import javassist.CtMethod;

public interface LumberFilter extends LumberStrategy {
	boolean mightTransform(String className);

	boolean shouldTransform(CtMethod method);

	default Predicate<CtMethod> transformTest() {
		return this::shouldTransform;
	}
}
