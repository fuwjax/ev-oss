package org.echovantage.tcp;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.SYNC;
import static org.echovantage.util.Streams.over;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
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
import java.util.function.Supplier;

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
		private final class SysOutChannel implements WritableByteChannel {
			@Override
			public boolean isOpen() {
				return true;
			}

			@Override
			public void close() throws IOException {
				// do nothing
			}

			@Override
			public int write(final ByteBuffer src) throws IOException {
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final WritableByteChannel out = Channels.newChannel(baos);
				out.write(src);
				out.close();
				final BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));
				for(final String line : over(in.lines())) {
					System.out.println(name + "-- " + line);
				}
				return src.position();
			}
		}

		private Writer response;
		private Writer log;
		private final Writer self;
		private SelectionKey key;
		private boolean closed;
		private final String name;
		private final int id;
		private ReadableByteChannel channel;

		public Receiver() {
			id = index.getAndIncrement();
			name = String.format("c2s.%04d", id);
			self = new Writer(this::channel, false);
		}

		private Receiver(final Writer response, final int id) throws IOException {
			this.response = response;
			this.id = id;
			name = String.format("s2c.%04d", id);
			self = new Writer(this::channel, true);
			log = initLog();
		}

		private Writer initLog() throws IOException {
			return new Writer(FileChannel.open(logPath.resolve(name), APPEND, CREATE, SYNC));
			// return new Writer(new SysOutChannel());
		}

		@Override
		public void connect(final SelectionKey key) throws IOException {
			this.key = key;
			channel = (ReadableByteChannel) key.channel();
			System.out.println("CONNECT: " + name + " -- " + name());
			if(log == null) {
				final Receiver server = new Receiver(self, id);
				select.connect(remote, () -> server);
				response = server.self;
				log = initLog();
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
			log.flush();
			final int flushed = response.flush();
			if(flushed > 0) {
				System.out.println("FLUSH:  " + name + " -- " + flushed);
			}
			int count = 0;
			while(read(count)) {
				self.lock();
				log.write(buffer.slice());
				response.write(buffer);
				count += buffer.position();
			}
			if(count > 0) {
				System.out.println("READ:  " + name + " -- " + count + " x" + response.size());
			}
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
			final int count = self.size();
			if(count > 0) {
				System.out.println("BUFFER:  " + name + " -- " + count);
			}
			return self.pendingWrites();
		}

		@Override
		public void write(final SelectionKey key) throws IOException {
			final int flushed = self.flush();
			if(flushed > 0) {
				System.out.println("WRITE:   " + name + " -- " + flushed);
			}
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public void close() throws IOException {
			if(!closed) {
				closed = true;
				System.out.println("CLOSE:   " + name);
				key.cancel();
				log.close();
				response.close();
			}
		}
	}

	private final AtomicInteger index = new AtomicInteger();
	private final Path logPath;
	private final InetSocketAddress remote;
	private final Select select = new Select();
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

	public TeeCP(final int serverPort, final String remoteHost, final int remotePort, final Path logPath) throws IOException {
		super();
		remote = new InetSocketAddress(remoteHost, remotePort);
		this.logPath = logPath;
		select.bind(new InetSocketAddress(serverPort), Receiver::new);
	}

	public void run() {
		select.run();
	}

	public static void main(final String... args) throws IOException {
		final Path tmp = Paths.get("/tmp/teecp");
		Files.createDirectories(tmp);
		final TeeCP tcp = new TeeCP(8085, "echovantage.com", 80, tmp);
		tcp.run();
	}
}
