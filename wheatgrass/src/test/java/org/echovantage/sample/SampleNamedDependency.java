package org.echovantage.sample;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by fuwjax on 3/3/15.
 */
public class SampleNamedDependency {
    @Inject
    @Named("second")
    private SampleResource resource;

    public SampleResource resource(){
        return resource;
    }
}
