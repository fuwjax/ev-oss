package org.echovantage.sample;

public class SampleModule {

    public final SampleResource publicResource = new SampleResource(7);
	private final SampleResource privateResource = new SampleResource(8);
	protected final SampleResource protectedResource = new SampleResource(9);
	final SampleResource packageResource = new SampleResource(10);
}
