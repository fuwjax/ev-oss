package org.echovantage.sample;

import org.echovantage.inject.Inject;

import java.util.Objects;

/**
 * Created by fuwjax on 2/15/15.
 */
public class SampleConstructorInject {
    private final int id;
    private final String name;

    @Inject
    public SampleConstructorInject(int id, String name){
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SampleConstructorInject)){
            return false;
        }
        SampleConstructorInject o = (SampleConstructorInject)obj;
        return Objects.equals(getClass(), o.getClass()) && id == o.id && Objects.equals(name, o.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
