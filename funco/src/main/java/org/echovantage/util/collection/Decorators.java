package org.echovantage.util.collection;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Decorators {
    public static <K, O, T> Function<Map<K, ? extends O>, Map<K, ? extends T>> decorateMap(
            Function<? super O, ? extends T> encoder){
        return map -> new MapDecorator<>(map, encoder);
    }

    public static <O, T> Function<List<? extends O>, List<? extends T>> decorateList(
            Function<? super O, ? extends T> encoder){
        return list -> new ListDecorator<>(list, encoder);
    }
}
