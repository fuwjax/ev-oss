package org.echovantage.util;

import org.echovantage.util.function.Functions;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.echovantage.util.function.Functions.supplier;

/**
 * Created by fuwjax on 3/4/15.
 */
public class Objects2 {
    public static <T> T coalesce(T... objects){
        for(T object: objects){
            if(object != null){
                return object;
            }
        }
        return null;
    }

    public static <T, U> T nullIf(U maybe, Function<U, T> ifNotNull, Supplier<T> ifNull) {
        return maybe == null ? ifNull.get() : ifNotNull.apply(maybe);
    }

    public static <T, U> T nullIf(U maybe, Function<U, T> ifNotNull) {
        return nullIf(maybe, ifNotNull, () -> null);
    }
}
