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
package org.echovantage.wonton.standard;

import org.echovantage.util.collection.DefaultAccessListDecorator;
import org.echovantage.util.collection.DefaultAccessMapDecorator;
import org.echovantage.util.collection.ListDecorator;
import org.echovantage.util.collection.MapDecorator;
import org.echovantage.wonton.Wonton;

import java.util.*;

public class RelaxedWonton implements Wonton {
    public static final Wonton RELAXED_NULL = new RelaxedWonton(NULL);
    public static Wonton relaxed(Wonton wonton) {
        return wonton == null ? RELAXED_NULL : wonton instanceof RelaxedWonton ? (RelaxedWonton) wonton : new RelaxedWonton(wonton);
    }

    private final Wonton wonton;

    public RelaxedWonton(Wonton wonton) {
        this.wonton = wonton;
    }

    @Override
    public String asString() {
        switch (wonton.type()) {
            case BOOLEAN:
            case NUMBER:
                return String.valueOf(value());
            case STRING:
            case VOID:
            case ARRAY:
            case STRUCT:
                return one().asString();
        }
        throw new InvalidTypeException();
    }

    private Wonton one() {
        switch (wonton.type()) {
            case ARRAY:
                List<? extends Wonton> array = wonton.asArray();
                if (array.size() == 1) {
                    return relaxed(array.get(0));
                }
                break;
            case STRUCT:
                Map<String, ? extends Wonton> map = wonton.asStruct();
                if (map.size() == 1) {
                    return relaxed(map.values().iterator().next());
                }
                break;
            default:
                return wonton;
        }
        throw new InvalidTypeException();
    }

    @Override
    public Boolean asBoolean() {
        switch (wonton.type()) {
            case NUMBER:
                return wonton.asDouble() != 0.;
            case STRING:
                return "true".equalsIgnoreCase(wonton.asString());
            case BOOLEAN:
            case VOID:
            case ARRAY:
            case STRUCT:
                return one().asBoolean();
        }
        throw new InvalidTypeException();
    }

    @Override
    public Number asNumber() {
        switch (wonton.type()) {
            case BOOLEAN:
                return wonton.asBoolean() ? 1 : 0;
            case STRING:
                try {
                    return Double.parseDouble(wonton.asString());
                } catch (NumberFormatException e) {
                    // continue
                }
            case NUMBER:
            case VOID:
            case ARRAY:
            case STRUCT:
                return one().asNumber();
        }
        throw new InvalidTypeException();
    }

    @Override
    public Map<String, ? extends Wonton> asStruct() {
        Map<String, Wonton> map;
        switch (wonton.type()) {
            case BOOLEAN:
            case STRING:
            case NUMBER:
            case VOID:
                map = Collections.singletonMap("value", this);
                break;
            case ARRAY:
                map = new LinkedHashMap<>();
                int[] index = new int[1];
                wonton.asArray().forEach(e -> map.put(Integer.toString(index[0]++), relaxed(e)));
                break;
            case STRUCT:
                map = new MapDecorator<>(wonton.asStruct(), RelaxedWonton::relaxed);
                break;
            default:
                throw new InvalidTypeException();
        }
        return new DefaultAccessMapDecorator<>(map, key -> NULL);
    }

    @Override
    public List<? extends Wonton> asArray() {
        List<Wonton> list;
        switch (wonton.type()) {
            case BOOLEAN:
            case STRING:
            case NUMBER:
            case VOID:
                list = Collections.singletonList(this);
                break;
            case ARRAY:
                list = new ListDecorator<>(wonton.asArray(), RelaxedWonton::relaxed);
                break;
            case STRUCT:
                list = new ListDecorator<>(new ArrayList<>(wonton.asStruct().values()), RelaxedWonton::relaxed);
                break;
            default:
                throw new InvalidTypeException();
        }
        return new DefaultAccessListDecorator<>(list, key -> NULL);
    }

    @Override
    public Object value() {
        return wonton.value();
    }

    @Override
    public Type type() {
        return wonton.type();
    }

    @Override
    public Wonton get(String key) {
        if (StandardPath.isPath(key)) {
            return get(StandardPath.path(key));
        }
        return relaxed(wonton.get(key));
    }

    @Override
    public void accept(Visitor visitor) {
        wonton.accept((p, w) -> visitor.visit(p, relaxed(w)));
    }

    @Override
    public String toString() {
        return wonton.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return wonton.equals(obj);
    }

    @Override
    public int hashCode() {
        return wonton.hashCode();
    }
}
