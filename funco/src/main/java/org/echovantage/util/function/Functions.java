/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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
package org.echovantage.util.function;

import java.util.function.*;

public class Functions {
	public static <T> Supplier<T> defer(Supplier<T> supplier){
		return new Deferred<>(supplier);
	}

	public static <T, U> UnsafeBiConsumer<T, U> biConsumer(final UnsafeBiConsumer<T, U> biConsumer) {
		return biConsumer;
	}

	public static <T, U, R> UnsafeBiFunction<T, U, R> biFunction(final UnsafeBiFunction<T, U, R> biFunction) {
		return biFunction;
	}

	public static <T> UnsafeBinaryOperator<T> binaryOperator(final UnsafeBinaryOperator<T> binaryOperator) {
		return binaryOperator;
	}

	public static <T, U> UnsafeBiPredicate<T, U> biPredicate(final UnsafeBiPredicate<T, U> biPredicate) {
		return biPredicate;
	}

	public static <T> UnsafeConsumer<T> consumer(final UnsafeConsumer<T> consumer) {
		return consumer;
	}

	public static UnsafeIntConsumer intConsumer(final UnsafeIntConsumer intConsumer) {
		return intConsumer;
	}

	public static <T, R> UnsafeFunction<T, R> function(final UnsafeFunction<T, R> function) {
		return function;
	}

	public static <T> UnsafePredicate<T> predicate(final UnsafePredicate<T> predicate) {
		return predicate;
	}

	public static UnsafeRunnable runnable(final UnsafeRunnable runnable) {
		return runnable;
	}

	public static <T> UnsafeSupplier<T> supplier(final UnsafeSupplier<T> supplier) {
		return supplier;
	}

	public static UnsafeIntSupplier intSupplier(final UnsafeIntSupplier intSupplier) {
		return intSupplier;
	}

	public static <T> UnsafeUnaryOperator<T> unaryOperator(final UnsafeUnaryOperator<T> unaryOperator) {
		return unaryOperator;
	}

	/* Eclipse is ridiculous about overloading generics right now... */
//	public static <T> Supplier<T> defer(UnsafeSupplier<T> supplier){
//		return new Deferred<>(supplier);
//	}
//	
//	public static <T, U> BiConsumer<T, U> biConsumer(final BiConsumer<T, U> biConsumer) {
//		return biConsumer;
//	}
//	
//	public static <T, U, R> BiFunction<T, U, R> biFunction(final BiFunction<T, U, R> biFunction) {
//		return biFunction;
//	}
//	
//	public static <T> BinaryOperator<T> binaryOperator(final BinaryOperator<T> binaryOperator) {
//		return binaryOperator;
//	}
//	
//	public static <T, U> BiPredicate<T, U> biPredicate(final BiPredicate<T, U> biPredicate) {
//		return biPredicate;
//	}
//	
//	public static <T> Consumer<T> consumer(final Consumer<T> consumer) {
//		return consumer;
//	}
//	
//	public static <T, R> Function<T, R> function(final Function<T, R> function) {
//		return function;
//	}
//	
//	public static IntConsumer intConsumer(final IntConsumer intConsumer) {
//		return intConsumer;
//	}
//	
//	public static IntSupplier intSupplier(final IntSupplier intSupplier) {
//		return intSupplier;
//	}
//
//	public static <T> Predicate<T> predicate(final Predicate<T> predicate) {
//		return predicate;
//	}
//	
//	public static Runnable runnable(final Runnable runnable) {
//		return runnable;
//	}
//
//	public static <T> Supplier<T> supplier(final Supplier<T> supplier) {
//		return supplier;
//	}
//
//	public static <T> UnaryOperator<T> unaryOperator(final UnaryOperator<T> unaryOperator) {
//		return unaryOperator;
//	}
}
