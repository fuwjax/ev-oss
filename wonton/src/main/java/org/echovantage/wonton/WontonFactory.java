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
package org.echovantage.wonton;

import org.echovantage.util.Lists;
import org.echovantage.util.collection.ListDecorator;
import org.echovantage.util.collection.MapDecorator;
import org.echovantage.wonton.Wonton.WArray;
import org.echovantage.wonton.Wonton.WNumber;
import org.echovantage.wonton.Wonton.WString;
import org.echovantage.wonton.Wonton.WStruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.echovantage.wonton.Wonton.NULL;

public interface WontonFactory {
    WontonFactory FACTORY = new WontonFactory() {
    };

    public interface Wontonable {
        Wonton toWonton();
    }

    public interface MutableWonton extends Wonton {
        void set(Path path, Wonton value);

        default void set(String path, Wonton value) {
            set(Path.path(path), value);
        }

        default MutableWonton with(Path path, Wonton value) {
            set(path, value);
            return this;
        }

        default MutableWonton with(String path, Wonton value) {
            set(path, value);
            return this;
        }
    }

    public interface MutableStruct extends MutableWonton, WStruct {
        default void set(Path path, Wonton value) {
            assert path != null && !path.isEmpty();
            if (path.tail().isEmpty()) {
                asStruct().put(path.key(), value);
            } else {
                Wonton wonton = get(path.key());
                if (wonton == null) {
                    MutableWonton mutable;
                    if ("0".equals(path.tail().key())) {
                        mutable = newArray(ArrayList::new);
                    } else {
                        mutable = newStruct(LinkedHashMap::new);
                    }
                    asStruct().put(path.key(), mutable);
                    mutable.set(path.tail(), value);
                } else if (wonton instanceof MutableWonton) {
                    ((MutableWonton) wonton).set(path.tail(), value);
                } else {
                    throw new IllegalArgumentException("Cannot set " + path + ", immutable value");
                }
            }
        }

        @Override
        Map<String, Wonton> asStruct();

        default MutableStruct with(String path, Wonton value) {
            set(path, value);
            return this;
        }

        default MutableStruct with(Path path, Wonton value) {
            set(path, value);
            return this;
        }
    }

    public interface MutableArray extends WArray, MutableWonton {
        default MutableArray append(final Wonton value) {
            asArray().add(value);
            return this;
        }

        @Override
        List<Wonton> asArray();

        @Override
        default void set(Path path, Wonton value) {
            assert path != null && !path.isEmpty();
            if (path.tail().isEmpty()) {
                set(path.key(), value);
            } else {
                Wonton wonton = get(path.key());
                if (wonton == null) {
                    MutableWonton mutable;
                    if ("0".equals(path.tail().key())) {
                        mutable = newArray(ArrayList::new);
                    } else {
                        mutable = newStruct(LinkedHashMap::new);
                    }
                    set(Integer.parseInt(path.key()), mutable);
                    mutable.set(path.tail(), value);
                } else if (wonton instanceof MutableWonton) {
                    ((MutableWonton) wonton).set(path.tail(), value);
                } else {
                    throw new IllegalArgumentException("Cannot set " + path + ", immutable value");
                }
            }
        }

        default void set(int index, Wonton value) {
            if(index == asArray().size()){
                asArray().add(value);
            }else {
                asArray().set(index, value);
            }
        }
    }

    public static MutableStruct newStruct(Map<String, Wonton> map) {
        return () -> map;
    }

    public static MutableArray newArray(List<Wonton> list) {
        return () -> list;
    }

    public static MutableStruct newStruct(Supplier<? extends Map<String, Wonton>> supplier) {
        return newStruct(supplier.get());
    }

    public static MutableArray newArray(Supplier<? extends List<Wonton>> supplier) {
        return newArray(supplier.get());
    }

    default Wonton wontonOf(CharSequence value) {
        if (value == null) {
            return NULL;
        }
        String string = value.toString();
        return (WString) () -> string;
    }

    default Wonton wontonOf(Number value) {
        return value == null ? NULL : (WNumber) () -> value;
    }

    default Wonton wontonOf(Boolean value) {
        return value == null ? NULL : value ? Wonton.TRUE : Wonton.FALSE;
    }

    default Wonton wontonOf(Map<String, ?> value) {
        if (value == null) {
            return NULL;
        }
        Map<String, Wonton> map = new MapDecorator<>(value, this::wontonOf);
        return (WStruct) () -> map;
    }

    default Wonton wontonOf(List<?> value) {
        if (value == null) {
            return NULL;
        }
        List<Wonton> list = new ListDecorator<>(value, this::wontonOf);
        return (WArray) () -> list;
    }

    default Wonton wontonOf(Stream<?> stream) {
        if (stream == null) {
            return NULL;
        }
        List<Wonton> list = stream.map(this::wontonOf).collect(Collectors.toList());
        return (WArray) () -> list;
    }

    default boolean canWonton(Class<?> type) {
        return Wonton.class.isAssignableFrom(type) ||
                Wontonable.class.isAssignableFrom(type) ||
                type.isPrimitive() ||
                Boolean.class.equals(type) ||
                Number.class.isAssignableFrom(type) ||
                CharSequence.class.isAssignableFrom(type) ||
                Map.class.isAssignableFrom(type) ||
                Iterable.class.isAssignableFrom(type) ||
                Stream.class.isAssignableFrom(type) ||
                type.isArray();
    }

    default Wonton wontonOf(Object object) {
        if (object == null) {
            return NULL;
        }
        if (object instanceof Wonton) {
            return (Wonton) object;
        }
        if (object instanceof Wontonable) {
            return ((Wontonable) object).toWonton();
        }
        if (object instanceof Boolean) {
            return wontonOf((Boolean) object);
        }
        if (object instanceof Number) {
            return wontonOf((Number) object);
        }
        if (object instanceof CharSequence) {
            return wontonOf(object.toString());
        }
        if (object instanceof Map) {
            return wontonOf((Map<String, ?>) object);
        }
        if (object instanceof Iterable) {
            return wontonOf(Lists.toList((Iterable<?>) object));
        }
        if (object instanceof Stream) {
            return wontonOf((Stream<?>) object);
        }
        if (object instanceof Object[]) {
            return wontonOf(Arrays.asList((Object[]) object));
        }
        if (object.getClass().isArray()) {
            return wontonOf(Lists.reflectiveList(object));
        }
        assert !canWonton(object.getClass());
        throw new IllegalArgumentException("No standard transformation for " + object.getClass());

    }
}
