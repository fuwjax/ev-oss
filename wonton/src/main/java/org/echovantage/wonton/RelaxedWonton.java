package org.echovantage.wonton;

import org.echovantage.util.ListDecorator;
import org.echovantage.util.MapDecorator;
import org.echovantage.wonton.standard.StandardPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RelaxedWonton implements Wonton {
    public static Wonton relaxed(Wonton wonton) {
        return wonton instanceof RelaxedWonton ? (RelaxedWonton) wonton : new RelaxedWonton(wonton);
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
                return this;
        }
        throw new InvalidTypeException();
    }

    @Override
    public Boolean asBoolean() {
        switch (wonton.type()) {
            case NUMBER:
                return wonton.asNumber().doubleValue() != 0.;
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
        switch (wonton.type()) {
            case BOOLEAN:
            case STRING:
            case NUMBER:
            case VOID:
                return Collections.singletonMap("value", this);
            case ARRAY:
                Map<String, Wonton> map = new LinkedHashMap<>();
                int[] index = new int[1];
                wonton.asArray().forEach(e -> map.put(Integer.toString(index[0]++), relaxed(e)));
                return map;
            case STRUCT:
                return new MapDecorator<>(wonton.asStruct(), RelaxedWonton::relaxed);
        }
        throw new InvalidTypeException();
    }

    @Override
    public List<? extends Wonton> asArray() {
        switch (wonton.type()) {
            case BOOLEAN:
            case STRING:
            case NUMBER:
            case VOID:
                return Collections.singletonList(this);
            case ARRAY:
                return new ListDecorator<>(wonton.asArray(), RelaxedWonton::relaxed);
            case STRUCT:
                return new ListDecorator<>(new ArrayList<>(wonton.asStruct().values()), RelaxedWonton::relaxed);
        }
        throw new InvalidTypeException();
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
        return relaxed(get(key));
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
