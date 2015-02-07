/**
 * Copyright (C) 2014 EchoVantage (info@echovantage.com)
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
import org.echovantage.wonton.standard.AbstractListWonton;
import org.echovantage.wonton.standard.AbstractMapWonton;
import org.echovantage.wonton.standard.ListWonton;
import org.echovantage.wonton.standard.MapWonton;
import org.echovantage.wonton.standard.NumberWonton;
import org.echovantage.wonton.standard.StringWonton;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface WontonFactory {
    WontonFactory FACTORY = new WontonFactory(){};

    public interface Wontonable {
        Wonton toWonton();
    }

    public interface MutableWonton extends Wonton {
        default void set(Wonton.Path path, Wonton value) {
            assert path != null && !path.isEmpty();
            if (path.tail().isEmpty()) {
                set(path.key(), value);
            } else {
                Wonton wonton = get(path.key());
                if (wonton == null) {
                    MutableWonton newWonton = factory().newStruct();
                    newWonton.set(path.tail(), value);
                    set(path.key(), newWonton);
                } else if (wonton instanceof MutableWonton) {
                    ((MutableWonton) wonton).set(path.tail(), value);
                } else {
                    throw new IllegalArgumentException("Cannot set " + path + ", immutable value");
                }
            }
        }

        WontonFactory factory();

        void set(final String key, final Wonton value);

        default MutableWonton with(String path, Object value) {
            set(Wonton.path(path), factory().wontonOf(value));
            return this;
        }

        default MutableWonton newStruct(String path){
            MutableWonton wonton = factory().newStruct();
            set(Wonton.path(path), wonton);
            return wonton;
        }

        default MutableArray newArray(String path){
            MutableArray wonton = factory().newArray();
            set(Wonton.path(path), wonton);
            return wonton;
        }
    }

    public interface MutableArray extends MutableWonton{
        MutableArray append(final Wonton value);

        Wonton get(int index);

        void set(int index, Wonton value);

        @Override
        default void set(final String key, final Wonton value){
            try {
                set(Integer.parseInt(key), value);
            }catch(NumberFormatException e){
                throw new NoSuchPathException(e);
            }
        }
    }

    default MutableWonton newStruct(){
        return new MapWonton(this);
    }

    default MutableArray newArray(){
        return new ListWonton(this);
    }

    default Wonton wontonOf(CharSequence value){
        return value == null ? Wonton.NULL : new StringWonton(value.toString());
    }

    default Wonton wontonOf(Number value){
        return value == null ? Wonton.NULL : new NumberWonton(value);
    }

    default Wonton wontonOf(Boolean value){
        return value == null ? Wonton.NULL : value ? Wonton.TRUE : Wonton.FALSE;
    }

    default Wonton wontonOf(Map<String, ?> value){
        return AbstractMapWonton.wrap(new MapDecorator<>(value, this::wontonOf));
    }

    default Wonton wontonOf(List<?> list) {
        return AbstractListWonton.wrap(new ListDecorator<>(list, this::wontonOf));
    }

    default Wonton wontonOf(Stream<?> stream) { return AbstractListWonton.wrap(stream.map(this::wontonOf).collect(Collectors.toList())); }

    default boolean canWonton(Class<?> type){
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

    default Wonton wontonOf(Object object){
        if(object == null) {
            return Wonton.NULL;
        }
        if(object instanceof Wonton) {
            return (Wonton) object;
        }
        if(object instanceof Wontonable){
            return ((Wontonable)object).toWonton();
        }
        if(object instanceof Boolean) {
            return wontonOf((Boolean) object);
        }
        if(object instanceof Number) {
            return wontonOf((Number) object);
        }
        if(object instanceof CharSequence) {
            return wontonOf(object.toString());
        }
        if(object instanceof Map) {
            return wontonOf((Map<String, ?>) object);
        }
        if(object instanceof Iterable) {
            return wontonOf(Lists.toList((Iterable<?>) object));
        }
        if(object instanceof Stream){
            return wontonOf((Stream<?>)object);
        }
        if(object instanceof Object[]) {
            return wontonOf(Arrays.asList((Object[]) object));
        }
        if(object.getClass().isArray()) {
            return wontonOf(Lists.reflectiveList(object));
        }
        assert !canWonton(object.getClass());
        throw new IllegalArgumentException("No standard transformation for " + object.getClass());

    }
}
