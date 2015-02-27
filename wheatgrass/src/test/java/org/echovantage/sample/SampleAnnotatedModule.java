package org.echovantage.sample;

import javax.inject.Named;
import java.util.function.Supplier;

public class SampleAnnotatedModule {
    @Named("first")
    public final SampleResource publicResource = new SampleResource(7);
    @Named("second")
	public final SampleResource privateResource = new SampleResource(8);
}
