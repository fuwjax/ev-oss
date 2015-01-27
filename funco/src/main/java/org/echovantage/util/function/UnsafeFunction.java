package org.echovantage.util.function;

import java.util.function.Function;

public interface UnsafeFunction<T, R> extends Function<T, R> {
	R applyUnsafe(T t) throws Exception;

	@Override
	default R apply(final T t) {
		try {
			return applyUnsafe(t);
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw new UnsafeException(e, "function did not apply safely");
		}
	}

	default UnsafeSupplier<R> defer(T value) {
		return () -> applyUnsafe(value);
	}
}
