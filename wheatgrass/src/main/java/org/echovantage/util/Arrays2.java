package org.echovantage.util;

import java.util.function.Function;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Arrays2 {
    public static <F, T> T[] transform(F[] source, Function<? super F, T> transform) {
        final T[] result = (T[]) new Object[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = transform.apply(source[i]);
        }
        return result;

    }
}
