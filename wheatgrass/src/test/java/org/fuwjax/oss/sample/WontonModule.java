package org.fuwjax.oss.sample;

import java.net.InetSocketAddress;

public class WontonModule {
	public static InetSocketAddress address(String hostname, int port) {
		return new InetSocketAddress(hostname, port);
	}
}
