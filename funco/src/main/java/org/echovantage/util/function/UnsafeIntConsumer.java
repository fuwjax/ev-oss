package org.echovantage.util.function;

import java.util.function.IntConsumer;

public interface UnsafeIntConsumer extends IntConsumer {
	void acceptUnsafe(int t) throws Exception;

	@Override
	default void accept(final int t) {
		try {
			acceptUnsafe(t);
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException(e, "consumer did not accept int safely");
		}
	}
}
