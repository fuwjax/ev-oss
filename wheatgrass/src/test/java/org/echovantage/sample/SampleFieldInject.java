package org.echovantage.sample;


import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by fuwjax on 2/15/15.
 */
public class SampleFieldInject {
    @Inject
    private int id;
    @Inject
    private String name;

    private SampleFieldInject(){
        // for injection
    }

    public SampleFieldInject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SampleFieldInject)){
            return false;
        }
        SampleFieldInject o = (SampleFieldInject)obj;
        return Objects.equals(getClass(), o.getClass()) && id == o.id && Objects.equals(name, o.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
