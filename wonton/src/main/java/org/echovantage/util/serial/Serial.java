package org.echovantage.util.serial;

import java.io.IOException;

public class Serial {
	private final Appendable appender;

	public Serial(final Appendable appender) {
		this.appender = appender;
	}

	public Serial append(final int cp) throws IOException {
		if(cp <= 0xFFFF) {
			appender.append((char) cp);
		} else {
			appender.append((char) (cp - 0x10000 >>> 10 | 0xD800)).append((char) (cp & 0x3FF | 0xDC00));
		}
		return this;
	}

	public Serial append(final String string) throws IOException {
		appender.append(string);
		return this;
	}
}
