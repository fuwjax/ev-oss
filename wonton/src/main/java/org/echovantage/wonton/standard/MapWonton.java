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
