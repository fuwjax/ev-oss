package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.Wonton.Mutable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.*;

public class MapWonton extends AbstractMapWonton implements Mutable {
    private final Map<String, Wonton> entries = new LinkedHashMap<>();

    @Override
    public Map<String, Wonton> asStruct() {
        return unmodifiableMap(entries);
    }

    @Override
    public MapWonton append(final Wonton value) {
        final String key = Integer.toString(entries.size() - 1);
        if (entries.containsKey(key)) {
            throw new InvalidTypeException();
        }
        entries.put(key, value);
        return this;
    }

    @Override
    public Mutable getOrCreate(String key) {
        Wonton child = get(key);
        if (child == null) {
            child = new MapWonton();
            set(key, child);
        } else if (!(child instanceof Mutable)) {
            throw new IllegalArgumentException("Cannot mutate " + key);
        }
        return (Mutable) child;
    }

    @Override
    public MapWonton set(final String shallowKey, final Wonton value) {
        entries.put(shallowKey, value);
        return this;
    }

    @Override
    public Wonton build() {
        if (entries.isEmpty()) {
            return this;
        }
        if (entries.containsKey("0") && entries.containsKey(Integer.toString(entries.size() - 1))) {
            return AbstractListWonton.wrap(new ArrayList<>(entries.values()));
        }
        for (final Map.Entry<String, Wonton> entry : entries.entrySet()) {
            if (entry.getValue() instanceof Mutable) {
                entry.setValue(((Mutable) entry.getValue()).build());
            }
        }
        return this;
    }
}
