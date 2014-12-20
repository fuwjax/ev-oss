package org.echovantage.util.function;

import java.util.function.UnaryOperator;

public interface UnsafeUnaryOperator<T> extends UnaryOperator<T>, UnsafeFunction<T,T> {
	// simply extends both
}
