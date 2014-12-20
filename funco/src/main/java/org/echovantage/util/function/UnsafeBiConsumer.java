package org.echovantage.util.function;

import java.util.function.BiConsumer;

public interface UnsafeBiConsumer<T, U> extends BiConsumer<T, U> {
	void acceptUnsafe(T t, U u) throws Exception;

	@Override
	default void accept(final T t, final U u) {
		try {
			acceptUnsafe(t, u);
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException(e, "biConsumer did not accept safely");
		}
	}
}
