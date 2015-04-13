package org.echovantage.tcp;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.function.Supplier;

import org.echovantage.tcp.RingBuffer.Handler;
import org.echovantage.util.Log;

public class Selectitron implements Runnable, AutoCloseable {
	private static final int SELECTOR_TIMEOUT = 5000;
	private static final int SIZE = 1024;
	private static final int COUNT = 1024;
	private static final int BACKLOG = 100;

	public interface Target {
		void write(Handler data) throws IOException;

		void close() throws IOException;
	}

	public interface Receiver extends Handler {
		void connect(Target target) throws IOException;

		void close() throws IOException;
	}

	private class TargetImpl implements Target, Handler {
		private final Receiver receiver;
		private final SelectionKey key;
		private long lastWrite;

		public TargetImpl(final SelectionKey key, final Receiver receiver) {
			this.key = key;
			this.receiver = receiver;
		}

		public void connect() throws IOException {
			key.interestOps(key.interestOps() & ~OP_CONNECT);
			receiver.connect(this);
		}

		public void read() throws IOException {
			final ScatteringByteChannel channel = (ScatteringByteChannel)key.channel();
			inbound.insert(channel::read, receiver);
		}

		@Override
		public void write(final Handler serializer) throws IOException {
			lastWrite = outbound.insert(serializer, this);
			if(lastWrite >= 0) {
				key.interestOps(key.interestOps() | OP_WRITE);
				key.selector().wakeup();
			}
		}

		@Override
		public void accept(final ByteBuffer buffer) throws IOException {
			if(!key.isValid()) {
				throw new CancelledKeyException();
			}
			if(key.isWritable()) {
				final GatheringByteChannel channel = (GatheringByteChannel)key.channel();
				channel.write(buffer);
			}
		}

		public void cleanup(final long writeLimit) {
			if(key.isValid()) {
				if(lastWrite < writeLimit) {
					key.interestOps(key.interestOps() & ~OP_WRITE);
				} else {
					key.interestOps(key.interestOps() | OP_WRITE);
				}
			}
		}

		@Override
		public void close() throws IOException {
			if(key.isValid()) {
				key.cancel();
				receiver.close();
			}
		}
	}

	private final Selector selector;
	private final Log log = Log.SILENT;
	private RingBuffer inbound;
	private final RingBuffer outbound = new RingBuffer(COUNT, SIZE);

	public Selectitron() throws IOException {
		selector = Selector.open();
	}

	@Override
	public void run() {
		assert inbound == null;
		inbound = new RingBuffer(COUNT, SIZE);
		try {
			while(selector.isOpen() && !Thread.currentThread().isInterrupted()) {
				select();
				process();
				cleanup();
			}
		} catch(final IOException e) {
			log.abort(e, "Select failed, terminating selector loop");
		} finally {
			try {
				close();
			} catch(final IOException e) {
				log.warn(e, "Selector failed to close cleanly");
			}
		}
	}

	private void select() throws IOException {
		selector.select(SELECTOR_TIMEOUT);
	}

	private void cleanup() {
		for(final SelectionKey key : selector.keys()) {
			if(!key.isValid()) {
				close(key);
			}
		}
	}

	private void process() {
		final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
		while(iter.hasNext()) {
			process(iter.next());
			iter.remove();
		}
		final long writeLimit = outbound.consume();
		for(final SelectionKey key : selector.keys()) {
			final TargetImpl target = (TargetImpl)key.attachment();
			target.cleanup(writeLimit);
		}
	}

	private void process(final SelectionKey key) {
		if(key.isValid()) { // can we assume this?
			try {
				if(key.isAcceptable()) {
					accept(key);
				}
				if(key.isConnectable()) {
					connect(key);
				}
				if(key.isReadable()) {
					read(key);
				}
			} catch(final IOException e) {
				log.abort(e, "Op failed, closing channel");
				key.cancel();
			}
		}
	}

	public SocketAddress bind(final SocketAddress local, final Supplier<Receiver> receivers) throws IOException {
		final ServerSocketChannel channel = ServerSocketChannel.open();
		try {
			channel.configureBlocking(false);
			channel.socket().setReuseAddress(true);
			channel.bind(local, BACKLOG);
			final SelectionKey key = register(channel, OP_ACCEPT);
			key.attach(receivers);
			return channel.getLocalAddress();
		} catch(final IOException e) {
			channel.close();
			throw e;
		}
	}

	public SocketAddress connect(final SocketAddress remote, final Receiver receiver) throws IOException {
		final SocketChannel channel = SocketChannel.open();
		try {
			channel.configureBlocking(false);
			channel.connect(remote);
			final SelectionKey key = register(channel, OP_CONNECT | OP_READ);
			key.attach(receiver);
			return channel.getLocalAddress();
		} catch(final IOException e) {
			channel.close();
			throw e;
		}
	}

	protected SelectionKey register(final SelectableChannel channel, final int ops, final Object attachment) throws IOException {
		return channel.register(selector, ops, attachment);
	}

	protected SelectionKey register(final SelectableChannel channel, final int ops) throws IOException {
		return channel.register(selector, ops);
	}

	private void accept(final SelectionKey key) throws IOException {
		final ServerSocketChannel server = (ServerSocketChannel)key.channel();
		final Supplier<Receiver> receivers = (Supplier<Receiver>)key.attachment();
		SocketChannel channel = server.accept();
		while(channel != null) {
			try {
				channel.socket().setTcpNoDelay(true);
				channel.configureBlocking(false);
				final SelectionKey acceptKey = register(channel, OP_READ);
				final TargetImpl target = new TargetImpl(key, receivers.get());
				acceptKey.attach(target);
				target.connect();
			} catch(final IOException e) {
				log.abort(e, "Client channel configuration failed, closing channel");
				channel.close();
			}
			channel = server.accept();
		}
	}

	private void connect(final SelectionKey key) throws IOException {
		final SocketChannel channel = (SocketChannel)key.channel();
		final Receiver receiver = (Receiver)key.attachment();
		final TargetImpl target = new TargetImpl(key, receiver);
		key.attach(target);
		channel.finishConnect();
		target.connect();
	}

	private void read(final SelectionKey key) throws IOException {
		final TargetImpl target = (TargetImpl)key.attachment();
		target.read();
	}

	@Override
	public void close() throws IOException {
		try {
			selector.close();
		} finally {
			for(final SelectionKey key : selector.keys()) {
				close(key);
			}
		}
	}

	private void close(final SelectionKey key) {
		key.cancel();
		try {
			key.channel().close();
		} catch(final IOException e) {
			log.warn(e, "Channel did not close cleanly");
		} finally {
			final TargetImpl target = (TargetImpl)key.attachment();
			try {
				target.close();
			} catch(final IOException e) {
				log.warn(e, "Target did not close cleanly");
			}
		}
	}
}
