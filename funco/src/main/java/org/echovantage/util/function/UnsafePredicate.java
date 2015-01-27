package org.echovantage.util.function;

import java.util.function.BiFunction;
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

	default UnsafePredicate<T> withToString(BiFunction<T, Boolean, String> toString){
		return new UnsafePredicate<T>(){
			public T lastT;
			public boolean lastResult;

			@Override
			public boolean testUnsafe(T t) throws Exception {
				lastResult = UnsafePredicate.this.testUnsafe(t);
				lastT = t;
				return lastResult;
			}

			@Override
			public String toString() {
				return toString.apply(lastT, lastResult);
			}
		};
	}
}
