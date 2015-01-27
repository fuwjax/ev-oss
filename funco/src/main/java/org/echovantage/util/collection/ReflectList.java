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
