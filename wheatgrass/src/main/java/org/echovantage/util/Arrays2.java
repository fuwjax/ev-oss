package org.echovantage.util;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Comparator;
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
}
