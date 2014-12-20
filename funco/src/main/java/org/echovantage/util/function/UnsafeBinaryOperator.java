package org.echovantage.util.function;

import java.util.function.BinaryOperator;

public interface UnsafeBinaryOperator<T> extends BinaryOperator<T>, UnsafeBiFunction<T,T,T> {
	// simply extends both
}
