package org.echovantage.util.function;

import java.util.function.Predicate;

public interface UnsafePredicate<T> extends Predicate<T> {
	boolean testUnsafe(T t) throws Exception;

	@Override
	default boolean test(final T t) {
		try {
			return testUnsafe(t);
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException(e, "consumer did not accept safely");
		}
	}
}
