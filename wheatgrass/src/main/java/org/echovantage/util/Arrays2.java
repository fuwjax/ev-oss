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
package org.echovantage.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Arrays2 {
    public static <F, T> T[] transform(F[] source, T[] dest, Function<? super F, T> transform) {
        if(dest.length < source.length){
            dest = (T[])Array.newInstance(dest.getClass().getComponentType(), source.length);
        }
        for (int i = 0; i < source.length; i++) {
            dest[i] = transform.apply(source[i]);
        }
        return dest;

    }

    public static <E> Comparator<E[]> comparingArray(Comparator<E> elementComparator) {
        return Comparator.<E[]>comparingInt(a -> a.length).thenComparing((a1, a2) -> {
            for(int i=0;i<a1.length;i++) {
                int c = elementComparator.compare(a1[i], a2[i]);
                if(c != 0) {
                    return c;
                }
            }
            return 0;
        });
    }

    public static <F1, F2, T> T[] zip(F1[] source1, F2[] source2, T[] dest, BiFunction<F1, F2, T> transform) {
        assert source1 != null && source2 != null && dest != null;
        assert source1.length == source2.length && source1.length == dest.length;
        for(int i=0;i<dest.length;i++){
            dest[i] = transform.apply(source1[i], source2[i]);
        }
        return dest;
    }
}
