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
package org.echovantage.generic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * Created by fuwjax on 2/22/15.
 */
public class TypeInspector {
    private final Class<?> target;

    public TypeInspector(){
        assert !TypeInspector.class.equals(getClass());
        this.target = getClass();
    }

    public Type of(String name) throws NoSuchElementException {
        try{
            return target.getDeclaredField(name).getGenericType();
        }catch(NoSuchFieldException e){
            return method(name).getGenericReturnType();
        }
    }

    public Type of(String name, int arg) throws NoSuchElementException {
        return method(name).getGenericParameterTypes()[arg];
    }

    public Stream<Type> fields() {
        return Arrays.asList(target.getDeclaredFields()).stream().map(Field::getGenericType);
    }

    private Method method(String name) {
        return Arrays.asList(target.getDeclaredMethods()).stream().filter(m -> m.getName().equals(name)).findAny().get();
    }
}
