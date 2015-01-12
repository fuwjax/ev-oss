package org.echovantage.util;

import java.util.stream.Stream;

/**
 * Created by fuwjax on 1/11/15.
 */
public class Streams {
    public static <T> Iterable<T> over(Stream<T> stream){
        return stream::iterator;
    }
}
