package org.echovantage.util.function;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class Functions {
	public static <T> Supplier<T> defer(Supplier<T> supplier){
		return new Deferred<>(supplier);
	}

	public static <T> Supplier<T> defer(UnsafeSupplier<T> supplier){
		return new Deferred<>(supplier);
	}

	public static <T, U> BiConsumer<T, U> biConsumer(final UnsafeBiConsumer<T, U> biConsumer) {
		return biConsumer;
	}

	public static <T, U> BiConsumer<T, U> biConsumer(final BiConsumer<T, U> biConsumer) {
		return biConsumer;
	}

	public static <T, U, R> BiFunction<T, U, R> biFunction(final UnsafeBiFunction<T, U, R> biFunction) {
		return biFunction;
	}

	public static <T, U, R> BiFunction<T, U, R> biFunction(final BiFunction<T, U, R> biFunction) {
		return biFunction;
	}

	public static <T> BinaryOperator<T> binaryOperator(final UnsafeBinaryOperator<T> binaryOperator) {
		return binaryOperator;
	}

	public static <T> BinaryOperator<T> binaryOperator(final BinaryOperator<T> binaryOperator) {
		return binaryOperator;
	}

	public static <T, U> BiPredicate<T, U> biPredicate(final UnsafeBiPredicate<T, U> biPredicate) {
		return biPredicate;
	}

	public static <T, U> BiPredicate<T, U> biPredicate(final BiPredicate<T, U> biPredicate) {
		return biPredicate;
	}

	public static <T> Consumer<T> consumer(final UnsafeConsumer<T> consumer) {
		return consumer;
	}

	public static <T> Consumer<T> consumer(final Consumer<T> consumer) {
		return consumer;
	}

	public static <T, R> Function<T, R> function(final UnsafeFunction<T, R> function) {
		return function;
	}

	public static <T, R> Function<T, R> function(final Function<T, R> function) {
		return function;
	}

	public static <T> Predicate<T> predicate(final UnsafePredicate<T> predicate) {
		return predicate;
	}

	public static <T> Predicate<T> predicate(final Predicate<T> predicate) {
		return predicate;
	}

	public static Runnable runnable(final UnsafeRunnable runnable) {
		return runnable;
	}

	public static Runnable runnable(final Runnable runnable) {
		return runnable;
	}

	public static <T> Supplier<T> supplier(final UnsafeSupplier<T> supplier) {
		return supplier;
	}

	public static <T> Supplier<T> supplier(final Supplier<T> supplier) {
		return supplier;
	}

	public static <T> UnaryOperator<T> unaryOperator(final UnsafeUnaryOperator<T> unaryOperator) {
		return unaryOperator;
	}

	public static <T> UnaryOperator<T> unaryOperator(final UnaryOperator<T> unaryOperator) {
		return unaryOperator;
	}
}
