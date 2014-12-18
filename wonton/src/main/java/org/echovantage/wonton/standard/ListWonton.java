package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.WontonFactory;

import java.util.ArrayList;
import java.util.List;

public class ListWonton extends AbstractListWonton implements WontonFactory.MutableArray {
    private final List<Wonton> values = new ArrayList<>();
    private final WontonFactory factory;

    public ListWonton(WontonFactory factory){
        this.factory = factory;
    }

    @Override
    public List<Wonton> asArray() {
        return values;
    }

    @Override
    public Wonton get(int index) {
        return super.get(index);
    }

    @Override
    public void append(final Wonton wonton) {
        assert wonton != null;
        values.add(wonton);
    }

    @Override
    public WontonFactory factory() {
        return factory;
    }

    public void set(final int index, final Wonton value) {
        while (index > values.size()) {
            values.add(NULL);
        }
        if (index == values.size()) {
            values.add(value);
        } else {
            values.set(index, value);
        }
    }

    public ListWonton remove(final int index) {
        values.remove(index);
        return this;
    }
}
