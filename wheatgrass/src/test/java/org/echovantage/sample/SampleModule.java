package org.echovantage.sample;

import org.echovantage.inject.Injector;

import java.util.function.Supplier;

public class SampleModule {
    public final SampleResource publicResource = new SampleResource(7);
	private final SampleResource privateResource = new SampleResource(8);

    public Supplier<SampleResource> supplier(){
        return () -> privateResource;
    }
}
