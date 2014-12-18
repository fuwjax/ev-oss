package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.Wonton.Mutable;

import java.util.ArrayList;
import java.util.List;

public class ListWonton extends AbstractListWonton implements Mutable {
    private final List<Wonton> values = new ArrayList<>();

    @Override
    public List<Wonton> asArray() {
        return values;
    }

    @Override
    public ListWonton append(final Wonton wonton) {
        assert wonton != null;
        values.add(wonton);
        return this;
    }

    @Override
    public ListWonton set(final String key, final Wonton value) {
        return set(Integer.parseInt(key), value);
    }

    public ListWonton set(final int index, final Wonton value) {
        while (index > values.size()) {
            values.add(NULL);
        }
        if (index == values.size()) {
            values.add(value);
        } else {
            values.set(index, value);
        }
        return this;
    }

    @Override
    public Mutable getOrCreate(String key) {
        int index = Integer.parseInt(key);
        Wonton child = get(index);
        if (child == null) {
            child = new MapWonton();
            set(index, child);
        } else if (!(child instanceof Mutable)) {
            throw new IllegalArgumentException("Cannot mutate " + key);
        }
        return (Mutable) child;
    }

    public ListWonton remove(final int index) {
        values.remove(index);
        return this;
    }

    @Override
    public Wonton build() {
        return this;
    }
}
