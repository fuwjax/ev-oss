package org.echovantage.util.function;

import java.util.function.BiPredicate;

public interface UnsafeBiPredicate<T, U> extends BiPredicate<T, U> {
	boolean applyTest(T t, U u) throws Exception;

	@Override
	default boolean test(T t, U u){
		try {
			return applyTest(t, u);
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException(e, "biPredicate did not test safely");
		}
	}
}
