package org.fuwjax.oss.sample;

import java.net.SocketAddress;

public class WontonUser {
	public final String name;
	public final SocketAddress source;
	public final SocketAddress target;

	public WontonUser(String name, SocketAddress source, SocketAddress target) {
		this.name = name;
		this.source = source;
		this.target = target;
	}
}
