package org.echovantage.wonton.standard;

import org.echovantage.wonton.StandardType;


public class NullWonton extends AbstractWonton {
	public static final NullWonton NULL = new NullWonton();

	private NullWonton() {
		// singleton
	}

	@Override
	public Type type() {
		return StandardType.NULL;
	}
}
