package org.echovantage.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.echovantage.tcp.Selectitron.Receiver;
import org.echovantage.tcp.Selectitron.Target;
import org.echovantage.util.Buffers;

public abstract class AbstractReceiver implements Receiver {
	private Target target;

	@Override
	public void connect(final Target target) throws IOException {
		this.target = target;
	}

	public void write(final ByteBuffer buffer) throws IOException {
		target.write(packet -> Buffers.copy(buffer, packet));
	}

	public void write(final ReadableByteChannel channel) throws IOException {
		target.write(channel::read);
	}

	@Override
	public void close() throws IOException {
		target.close();
	}
}
