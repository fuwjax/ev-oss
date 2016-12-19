/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.util.function;

import java.util.function.*;

public class Functions2 {
	public static <T, U> UnsafeBiConsumer<T, U> f(final UnsafeBiConsumer<T, U> biConsumer) {
		return biConsumer;
	}

	public static <T, U, R> UnsafeBiFunction<T, U, R> f(final UnsafeBiFunction<T, U, R> biFunction) {
		return biFunction;
	}

	public static <T> UnsafeBinaryOperator<T> f(final UnsafeBinaryOperator<T> binaryOperator) {
		return binaryOperator;
	}

	public static <T, U> UnsafeBiPredicate<T, U> f(final UnsafeBiPredicate<T, U> biPredicate) {
		return biPredicate;
	}

	public static <T> UnsafeConsumer<T> f(final UnsafeConsumer<T> consumer) {
		return consumer;
	}

	public static UnsafeIntConsumer f(final UnsafeIntConsumer intConsumer) {
		return intConsumer;
	}

	public static <T, R> UnsafeFunction<T, R> f(final UnsafeFunction<T, R> function) {
		return function;
	}

	public static <T> UnsafePredicate<T> f(final UnsafePredicate<T> predicate) {
		return predicate;
	}

	public static UnsafeRunnable f(final UnsafeRunnable runnable) {
		return runnable;
	}

	public static <T> UnsafeSupplier<T> f(final UnsafeSupplier<T> supplier) {
		return supplier;
	}

	public static UnsafeIntSupplier f(final UnsafeIntSupplier intSupplier) {
		return intSupplier;
	}

	public static <T> UnsafeUnaryOperator<T> f(final UnsafeUnaryOperator<T> unaryOperator) {
		return unaryOperator;
	}

	public static <T, U> BiConsumer<T, U> f(final BiConsumer<T, U> biConsumer) {
		return biConsumer;
	}
	
	public static <T, U, R> BiFunction<T, U, R> f(final BiFunction<T, U, R> biFunction) {
		return biFunction;
	}
	
	public static <T> BinaryOperator<T> f(final BinaryOperator<T> binaryOperator) {
		return binaryOperator;
	}
	
	public static <T, U> BiPredicate<T, U> f(final BiPredicate<T, U> biPredicate) {
		return biPredicate;
	}
	
	public static <T> Consumer<T> f(final Consumer<T> consumer) {
		return consumer;
	}
	
	public static <T, R> Function<T, R> f(final Function<T, R> function) {
		return function;
	}
	
	public static IntConsumer f(final IntConsumer intConsumer) {
		return intConsumer;
	}
	
	public static IntSupplier f(final IntSupplier intSupplier) {
		return intSupplier;
	}

	public static <T> Predicate<T> f(final Predicate<T> predicate) {
		return predicate;
	}
	
	public static Runnable f(final Runnable runnable) {
		return runnable;
	}

	public static <T> Supplier<T> f(final Supplier<T> supplier) {
		return supplier;
	}

	public static <T> UnaryOperator<T> f(final UnaryOperator<T> unaryOperator) {
		return unaryOperator;
	}
}
