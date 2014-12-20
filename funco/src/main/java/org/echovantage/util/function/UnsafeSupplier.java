package org.echovantage.util.function;

import java.util.function.Supplier;

public interface UnsafeSupplier<T> extends Supplier<T> {
	T getUnsafe() throws Exception;

	@Override
	default T get() {
		try {
			return getUnsafe();
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException("supplier did not get safely", e);
		}
	}
}
