package org.echovantage.util.function;

import java.util.function.Consumer;

public interface UnsafeConsumer<T> extends Consumer<T> {
	void acceptUnsafe(T t) throws Exception;

	@Override
	default void accept(final T t) {
		try {
			acceptUnsafe(t);
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException("consumer did not accept safely", e);
		}
	}
}
