package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.WontonFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.*;

public class MapWonton extends AbstractMapWonton implements WontonFactory.MutableWonton {
    private final Map<String, Wonton> entries = new LinkedHashMap<>();
    private final WontonFactory factory;

    public MapWonton(WontonFactory factory){
        this.factory =factory;
    }

    @Override
    public Map<String, Wonton> asStruct() {
        return unmodifiableMap(entries);
    }

    @Override
    public WontonFactory factory() {
        return factory;
    }

    @Override
    public void set(final String key, final Wonton value) {
        entries.put(key, value);
    }
}
