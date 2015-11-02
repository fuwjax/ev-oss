package org.fuwjax.oss.lumber;

import java.util.Map;

public interface LumberStrategy {
	default void safeConfigure(final Map<String, Object> config) {
		try {
			configure(config.get(getClass().getCanonicalName()));
		} catch (final Exception e) {
			throw new RuntimeException("Could not configure " + getClass().getCanonicalName(), e);
		}
	}

	void configure(Object configData) throws Exception;
}
