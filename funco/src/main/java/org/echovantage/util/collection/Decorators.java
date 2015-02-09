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
