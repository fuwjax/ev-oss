package org.echovantage.util.function;

import java.util.function.BiFunction;

public interface UnsafeBiFunction<T, U, R> extends BiFunction<T, U, R> {
	R applyUnsafe(T t, U u) throws Exception;

	@Override
	default R apply(final T t, final U u) {
		try {
			return applyUnsafe(t, u);
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException("biFunction did not apply safely", e);
		}
	}
}
