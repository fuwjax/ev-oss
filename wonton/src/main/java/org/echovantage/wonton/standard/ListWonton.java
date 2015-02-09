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
    public ListWonton append(final Wonton wonton) {
        assert wonton != null;
        values.add(wonton);
        return this;
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
