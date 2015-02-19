package org.echovantage.sample;

public class SampleMethodModule {
    public SampleResource resource() {
        return new SampleResource(123);
    }
}
