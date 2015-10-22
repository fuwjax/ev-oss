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
package org.fuwjax.oss.util;

import org.fuwjax.oss.util.function.Functions;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.fuwjax.oss.util.function.Functions.supplier;

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

    public static Boolean inferBoolean(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof Boolean){
            return (Boolean)value;
        }
        if(value instanceof String){
            return Boolean.valueOf((String)value);
        }
        if(value instanceof Number){
            return ((Number)value).doubleValue() != 0;
        }
        throw new IllegalArgumentException("cannot infer boolean from "+value.getClass());
    }

    public static Number inferNumber(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof Boolean){
            return ((Boolean)value) ? 1 : 0;
        }
        if(value instanceof String){
            return new BigDecimal((String)value);
        }
        if(value instanceof Number){
            return ((Number)value);
        }
        throw new IllegalArgumentException("cannot infer number from "+value.getClass());
    }
}
