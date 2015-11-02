package org.fuwjax.oss.lumber;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import javassist.CtMethod;

public class SignatureFilter implements LumberFilter {
	private final Map<String, List<BiPredicate<String, String>>> methods = new HashMap<>();

	@Override
	public void configure(final Object configData) throws Exception {
		((List<String>) configData).forEach(this::add);
	}

	private void add(final String methodSpec) {
		final String[] split = methodSpec.replaceAll("\\s+", "").split("::", 2);
		if (split.length == 2) {
			methods.computeIfAbsent(split[0], k -> new ArrayList<>()).add(methodSpec(split[1]));
		}
	}

	private static BiPredicate<String, String> methodSpec(final String method) {
		if (method.equals("*")) {
			return (n, d) -> true;
		}
		if (method.contains("(")) {
			return (n, d) -> method.equals(n + d);
		}
		return (n, d) -> method.equals(n);
	}

	@Override
	public boolean mightTransform(final String className) {
		return methods.containsKey(className);
	}

	@Override
	public boolean shouldTransform(final CtMethod method) {
		return methods.getOrDefault(method.getDeclaringClass().getName(), emptyList()).stream()
				.anyMatch(spec -> spec.test(method.getName(), method.getSignature()));
	}
}
