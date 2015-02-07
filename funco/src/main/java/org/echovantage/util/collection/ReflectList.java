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
package org.echovantage.util.collection;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fuwjax on 1/25/15.
 */
public class ReflectList extends AbstractList<Object> {
    public static List<?> asList(Object value){
        assert value != null && value.getClass().isArray();
        if(Object[].class.isAssignableFrom(value.getClass())){
            return Arrays.asList((Object[])value);
        }
        return new ReflectList(value);
    }

    private final Object value;

    private ReflectList(Object value){
        assert value.getClass().getComponentType().isPrimitive();
        this.value = value;
    }

    @Override
    public Object get(int index) {
        return Array.get(value, index);
    }

    @Override
    public int size() {
        return Array.getLength(value);
    }
}
