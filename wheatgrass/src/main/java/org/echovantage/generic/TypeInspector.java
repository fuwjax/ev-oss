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
