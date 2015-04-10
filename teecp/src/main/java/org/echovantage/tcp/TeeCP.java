package org.echovantage.tcp;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.echovantage.util.Log;

public class TeeCP {
	private static final int SELECTOR_TIMEOUT = 5000;
	private static final int SIZE = 1024;
	private static final int COUNT = 1024;
	private static final int BACKLOG = 100;

	public static void main(final String... args) throws Exception {
		final TeeCP tcp = new TeeCP();
		tcp.run();
	}

	private interface Target {
		void write(ByteBuffer buffer);
	}

	private interface Receiver {
		void connect(Target target);
	}

	private volatile boolean alive = true;
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final Selector selector;
	private final Log log = Log.SILENT;
	private final ByteBuffer[] inboundBuffers;
	private final ByteBuffer[] outboundBuffers;
	private final Target[] inboundClaims;
	private final Target[] outboundClaims;
	private volatile long index;

	public TeeCP() throws IOException {
		selector = Selector.open();
		inboundBuffers = new ByteBuffer[COUNT];
		inboundClaims = new Target[COUNT];
		for(int i = 0; i < inboundBuffers.length; i++) {
			inboundBuffers[i] = ByteBuffer.allocate(SIZE);
		}
		outboundBuffers = new ByteBuffer[COUNT];
		outboundClaims = new Target[COUNT];
		for(int i = 0; i < outboundBuffers.length; i++) {
			outboundBuffers[i] = ByteBuffer.allocate(SIZE);
		}
	}

	public void run() {
		try {
			while(alive && !Thread.currentThread().isInterrupted()) {
				select();
				process();
				cleanup();
			}
		} catch(final IOException e) {
			log.abort(e, "Select failed, terminating selector loop");
		} finally {
			selectorClose();
		}
	}

	protected void select() throws IOException {
		selector.select(SELECTOR_TIMEOUT);
	}

	protected void cleanup() {
		for(final SelectionKey key : selector.keys()) {
			if(!key.isValid()) {
				close(key);
			}
		}
	}

	protected void process() {
		final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
		while(iter.hasNext()) {
			process(iter.next());
			iter.remove();
		}
	}

	protected void process(final SelectionKey key) {
		if(key.isValid()) { // can we assume this?
			try {
				if(key.isAcceptable()) {
					accept((ServerSocketChannel) key.channel(), (Supplier<Receiver>) key.attachment());
				}
				if(key.isConnectable()) {
					final int ops = connect((SocketChannel) key.channel(), (Receiver) key.attachment());
					key.interestOps(ops);
				}
				if(key.isReadable()) {
					final int ops = read((SocketChannel) key.channel(), (Receiver) key.attachment());
					key.interestOps(ops);
				}
				if(key.isWritable()) {
					write((SocketChannel) key.channel(), (Receiver) key.attachment());
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
			final SelectionKey key = register(channel, OP_CONNECT);
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

	private void accept(final ServerSocketChannel server, final Supplier<Receiver> receivers) throws IOException {
		SocketChannel channel = server.accept();
		while(channel != null) {
			try {
				channel.socket().setTcpNoDelay(true);
				channel.configureBlocking(false);
				final SelectionKey acceptKey = register(channel, OP_READ);
				final Receiver receiver = receivers.get();
				acceptKey.attach(receiver);
				acceptKey.interestOps(receiver.connect());
			} catch(final IOException e) {
				log.abort(e, "Client channel configuration failed, closing channel");
				channel.close();
			}
			channel = server.accept();
		}
	}

	private int connect(final SocketChannel channel, final Target target) throws IOException {
		channel.finishConnect();
		return target.connect();
	}

	private int read(final SocketChannel channel, final Target target) {
		channel.read(buffers[(int) (index % buffers.length)]);
		target.write(index);
	}

	private int write(final SocketChannel channel, final Target target) throws IOException {
		channel.read(buffers[(int) (index % buffers.length)]);
	}

	protected void selectorClose() {
		lock.lock();
		try {
			alive = false;
			for(final SelectionKey key : selector.keys()) {
				close(key);
			}
			selector.close();
			condition.signalAll();
		} catch(final IOException e) {
			log.warn(e, "Selector did not close cleanly");
		} finally {
			lock.unlock();
		}
	}

	protected void close(final SelectionKey key) {
		key.cancel();
		try {
			key.channel().close();
		} catch(final IOException e) {
			log.warn(e, "Channel did not close cleanly");
		}
	}
}
