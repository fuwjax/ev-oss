/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.tcp;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.function.Supplier;

import org.fuwjax.oss.util.Log;

public class Select implements Runnable, AutoCloseable {
	private static final int SELECTOR_TIMEOUT = 5000;
	private static final int BACKLOG = 100;

	public interface Handler extends AutoCloseable {
		void connect(SelectionKey key) throws IOException;

		void read(SelectionKey key) throws IOException;

		void write(SelectionKey key) throws IOException;

		boolean pendingWrites();
	}

	private final Selector selector;
	private final Log log;

	public Select(final Log log) throws IOException {
		this.log = log;
		selector = Selector.open();
	}

	@Override
	public void run() {
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
			} else if(key.attachment() instanceof Handler) {
				final Handler handler = (Handler) key.attachment();
				if(handler.pendingWrites()) {
					key.interestOps(OP_READ | OP_WRITE);
				} else {
					key.interestOps(OP_READ);
				}
			}
		}
	}

	private void process() {
		final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
		while(iter.hasNext()) {
			process(iter.next());
			iter.remove();
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
				if(key.isWritable()) {
					write(key);
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

	public SocketAddress bind(final SocketAddress local, final Supplier<Handler> handlers) throws IOException {
		final ServerSocketChannel channel = ServerSocketChannel.open();
		try {
			channel.configureBlocking(false);
			channel.socket().setReuseAddress(true);
			channel.bind(local, BACKLOG);
			channel.register(selector, OP_ACCEPT, handlers);
			return channel.getLocalAddress();
		} catch(final IOException e) {
			channel.close();
			throw e;
		}
	}

	public SocketAddress connect(final SocketAddress remote, final Supplier<Handler> handlers) throws IOException {
		final SocketChannel channel = SocketChannel.open();
		try {
			channel.configureBlocking(false);
			channel.connect(remote);
			channel.register(selector, OP_CONNECT | OP_READ | OP_WRITE, handlers);
			return channel.getLocalAddress();
		} catch(final IOException e) {
			channel.close();
			throw e;
		}
	}

	private void accept(final SelectionKey key) throws IOException {
		final ServerSocketChannel server = (ServerSocketChannel) key.channel();
		final Supplier<Handler> handlers = (Supplier<Handler>) key.attachment();
		SocketChannel channel = server.accept();
		while(channel != null) {
			try {
				channel.socket().setTcpNoDelay(true);
				channel.configureBlocking(false);
				final SelectionKey acceptKey = channel.register(selector, OP_READ | OP_WRITE);
				attach(acceptKey, handlers);
			} catch(final IOException e) {
				log.abort(e, "Client channel configuration failed, closing channel");
				channel.close();
			}
			channel = server.accept();
		}
	}

	private void connect(final SelectionKey key) throws IOException {
		final SocketChannel channel = (SocketChannel) key.channel();
		final Supplier<Handler> handlers = (Supplier<Handler>) key.attachment();
		channel.finishConnect();
		key.interestOps(key.interestOps() & ~OP_CONNECT);
		attach(key, handlers);
	}

	private void attach(final SelectionKey key, final Supplier<Handler> handlers) throws IOException {
		final Handler handler = handlers.get();
		key.attach(handler);
		handler.connect(key);
	}

	private void read(final SelectionKey key) throws IOException {
		final Handler handler = (Handler) key.attachment();
		handler.read(key);
	}

	private void write(final SelectionKey key) throws IOException {
		final Handler handler = (Handler) key.attachment();
		handler.write(key);
	}

	@Override
	public void close() throws IOException {
		try {
			for(final SelectionKey key : selector.keys()) {
				close(key);
			}
		} finally {
			selector.close();
		}
	}

	private void close(final SelectionKey key) {
		key.cancel();
		try {
			key.channel().close();
		} catch(final IOException e) {
			log.warn(e, "Channel did not close cleanly");
		} finally {
			if(key.attachment() instanceof AutoCloseable) {
				try {
					((AutoCloseable) key.attachment()).close();
				} catch(final Exception e) {
					log.warn(e, "Target did not close cleanly");
				}
			}
		}
	}
}
