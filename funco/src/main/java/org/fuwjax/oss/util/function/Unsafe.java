package org.fuwjax.oss.util.function;

public class Unsafe {
	public static <T, U> UnsafeBiConsumer<T, U> unsafe(final UnsafeBiConsumer<T, U> biConsumer) {
		return biConsumer;
	}

	public static <T, U, R> UnsafeBiFunction<T, U, R> unsafe(final UnsafeBiFunction<T, U, R> biFunction) {
		return biFunction;
	}

	public static <T> UnsafeBinaryOperator<T> unsafe(final UnsafeBinaryOperator<T> binaryOperator) {
		return binaryOperator;
	}

	public static <T, U> UnsafeBiPredicate<T, U> unsafe(final UnsafeBiPredicate<T, U> biPredicate) {
		return biPredicate;
	}

	public static <T> UnsafeConsumer<T> unsafe(final UnsafeConsumer<T> consumer) {
		return consumer;
	}

	public static UnsafeIntConsumer unsafe(final UnsafeIntConsumer intConsumer) {
		return intConsumer;
	}

	public static <T, R> UnsafeFunction<T, R> unsafe(final UnsafeFunction<T, R> function) {
		return function;
	}

	public static <T> UnsafePredicate<T> unsafe(final UnsafePredicate<T> predicate) {
		return predicate;
	}

	public static UnsafeRunnable unsafe(final UnsafeRunnable runnable) {
		return runnable;
	}

	public static <T> UnsafeSupplier<T> unsafe(final UnsafeSupplier<T> supplier) {
		return supplier;
	}

	public static UnsafeIntSupplier unsafe(final UnsafeIntSupplier intSupplier) {
		return intSupplier;
	}

	public static <T> UnsafeUnaryOperator<T> unsafe(final UnsafeUnaryOperator<T> unaryOperator) {
		return unaryOperator;
	}
}
