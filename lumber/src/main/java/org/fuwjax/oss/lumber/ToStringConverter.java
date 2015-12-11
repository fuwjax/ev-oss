package org.fuwjax.oss.lumber;

import java.util.Map;

public class ToStringConverter implements LumberNormalizer {
	@Override
	public void configure(final Object configData) throws Exception {
		// do nothing
	}

	@Override
	public boolean canNormalize(final Class<?> cls) {
		return !Number.class.isAssignableFrom(cls) && !Boolean.class.equals(cls) && !String.class.equals(cls)
				&& !Iterable.class.isAssignableFrom(cls) && !Map.class.isAssignableFrom(cls) && !cls.isArray();
	}

	@Override
	public String normalize(final Object obj) {
		return String.valueOf(obj);
	}
}
