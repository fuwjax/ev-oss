package org.fuwjax.oss.sample;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class WontonTestModule extends WontonModule {
	public final String name = "test";
	
	public SocketAddress source(int port) {
		return new InetSocketAddress("localhost", port);
	}
}
