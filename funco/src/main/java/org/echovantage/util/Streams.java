package org.echovantage.util;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Created by fuwjax on 1/11/15.
 */
public class Streams {
    public static <T> Iterable<T> over(Stream<T> stream){
        return stream::iterator;
    }

    public static <T> Stream<T> stream(Collection<T> c){
        return c == null ? Collections.<T>emptySet().stream() : c.stream();
    }
}
