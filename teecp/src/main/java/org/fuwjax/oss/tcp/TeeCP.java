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

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.SYNC;
import static org.fuwjax.oss.util.function.Functions.supplier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.fuwjax.oss.util.Log;
import org.fuwjax.oss.util.io.Files2;

public class TeeCP {
	private final class Writer {
		private final Supplier<WritableByteChannel> channelRef;
		private final List<ByteBuffer> pending = new LinkedList<>();
		private boolean force;
		private boolean lock;

		public Writer(final WritableByteChannel channel) {
			this(() -> channel, false);
		}

		public Writer(final Supplier<WritableByteChannel> channelRef, final boolean force) {
			this.channelRef = channelRef;
			this.force = force;
		}

		private void write(final ByteBuffer buffer) throws IOException {
			final WritableByteChannel channel = channelRef.get();
			if(channel != null && pending.isEmpty()) {
				if(lock) {
					lock = false;
					force = !force;
				}
				channel.write(buffer);
			}
			if(buffer.hasRemaining()) {
				final ByteBuffer local = ByteBuffer.allocate(buffer.remaining());
				local.put(buffer);
				local.flip();
				pending.add(local);
			}
		}

		public void close() throws IOException {
			if(!pending.isEmpty()) {
				try {
					flush();
				} catch(final IOException e) {
					// continue
				}
				if(!pending.isEmpty()) {
					System.err.println("closing but not flushed");
				}
			}
			final WritableByteChannel channel = channelRef.get();
			if(channel != null) {
				channel.close();
			}
		}

		public int flush() throws IOException {
			int count = 0;
			final WritableByteChannel channel = channelRef.get();
			if(channel != null) {
				final Iterator<ByteBuffer> iter = pending.iterator();
				while(iter.hasNext()) {
					final ByteBuffer next = iter.next();
					count += channel.write(next);
					if(next.hasRemaining()) {
						break;
					}
					iter.remove();
				}
			}
			return count;
		}

		public boolean pendingWrites() {
			return force || !pending.isEmpty();
		}

		public void lock() {
			if(!lock) {
				lock = true;
				force = !force;
			}
		}

		public int size() {
			return pending.stream().mapToInt(ByteBuffer::remaining).sum();
		}
	}

	private final class Receiver implements Select.Handler {
		private Writer response;
		private Writer audit;
		private final Writer self;
		private SelectionKey key;
		private boolean closed;
		private final String name;
		private final int id;
		private ReadableByteChannel channel;

		public Receiver() {
			id = index.getAndIncrement();
			name = String.format("request/%04d", id);
			self = new Writer(this::channel, false);
		}

		private Receiver(final Writer response, final int id) throws IOException {
			this.response = response;
			this.id = id;
			name = String.format("response/%04d", id);
			self = new Writer(this::channel, true);
			audit = initAudit();
		}

		private Writer initAudit() throws IOException {
			return new Writer(FileChannel.open(auditPath.resolve(name), APPEND, CREATE, SYNC));
		}

		@Override
		public void connect(final SelectionKey key) throws IOException {
			this.key = key;
			channel = (ReadableByteChannel) key.channel();
			log.info("CONNECT: %s -- %s ", name, supplier(this::name));
			if(audit == null) {
				final Receiver server = new Receiver(self, id);
				select.connect(remote, () -> server);
				response = server.self;
				audit = initAudit();
			} else if(pendingWrites() && key.isWritable()) {
				write(key);
			}
		}

		private String name() throws IOException {
			final SocketChannel channel = (SocketChannel) key.channel();
			return channel.getLocalAddress() + " -> " + channel.getRemoteAddress();
		}

		private WritableByteChannel channel() {
			return key != null && key.isValid() && key.isWritable() ? new WritableByteChannel() {
				private final WritableByteChannel channel = (WritableByteChannel) key.channel();

				@Override
				public boolean isOpen() {
					return channel.isOpen();
				}

				@Override
				public void close() throws IOException {
					key.cancel();
				}

				@Override
				public int write(final ByteBuffer src) throws IOException {
					return channel.write(src);
				}
			} : null;
		}

		@Override
		public void read(final SelectionKey key) throws IOException {
			audit.flush();
			final int flushed = response.flush();
			log.info(positive(flushed), "FLUSH: %s -- %d", name, flushed);
			int count = 0;
			while(read(count)) {
				self.lock();
				audit.write(buffer.slice());
				response.write(buffer);
				count += buffer.position();
			}
			log.info(positive(count), "READ: %s -- %d x%s", name, count, supplier(response::size));
		}

		private boolean read(final int count) throws IOException {
			buffer.clear();
			final int packet = channel.read(buffer);
			buffer.flip();
			if(packet == -1 && count == 0) {
				key.cancel();
			}
			return packet > 0;
		}

		@Override
		public boolean pendingWrites() {
			log.info(() -> self.size() > 0, "BUFFER: %s -- %s", name, supplier(self::size));
			return self.pendingWrites();
		}

		@Override
		public void write(final SelectionKey key) throws IOException {
			final int flushed = self.flush();
			log.info(positive(flushed), "WRITE: %s -- %d", name, flushed);
		}

		private BooleanSupplier positive(final int value) {
			return () -> value > 0;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public void close() throws IOException {
			if(!closed) {
				closed = true;
				log.info("CLOSE: %s", name);
				key.cancel();
				audit.close();
				response.close();
			}
		}
	}

	private final AtomicInteger index = new AtomicInteger();
	private final Path auditPath;
	private final InetSocketAddress remote;
	private final Log log;
	private final Select select;
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

	public TeeCP(final Log log, final int serverPort, final String remoteHost, final int remotePort, final Path auditPath) throws IOException {
		this.log = log;
		select = new Select(log);
		remote = new InetSocketAddress(remoteHost, remotePort);
		this.auditPath = auditPath;
		select.bind(new InetSocketAddress(serverPort), Receiver::new);
		Files.createDirectories(auditPath.resolve("request"));
		Files.createDirectories(auditPath.resolve("response"));
	}

	public void run() {
		select.run();
	}

	public static void main(final String... args) throws IOException {
		if(args.length < 4) {
			System.out.println("Usage: teecp.jar <local bind port> <remote host> <remote port> <tee path>");
			return;
		}
		final Path tmp = Paths.get(args[3]);
		Files2.delete(tmp);
		final TeeCP tcp = new TeeCP(Log.SYSTEM, Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]), tmp);
		tcp.run();
	}
}
