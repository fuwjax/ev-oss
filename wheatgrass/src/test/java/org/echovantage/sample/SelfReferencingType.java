package org.echovantage.sample;


import javax.inject.Inject;

/**
 * Created by fuwjax on 2/18/15.
 */
public class SelfReferencingType {
    @Inject
    private SelfReferencingType self;

    public SelfReferencingType getSelf(){
        return self;
    }
}
