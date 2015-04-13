package org.echovantage.tcp;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.SYNC;
import static org.echovantage.util.function.Functions.supplier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class TeeCP extends Selectitron {
	private final class ForwardingReceiver extends AbstractReceiver {
		private final ForwardingReceiver response;
		private final FileChannel log;

		public ForwardingReceiver() throws IOException {
			final int id = index.getAndIncrement();
			response = new ForwardingReceiver(this, id);
			TeeCP.this.connect(remote, response);
			log = FileChannel.open(logPath.resolve(String.format("input.%04d", id)), APPEND, CREATE_NEW, SYNC);
		}

		private ForwardingReceiver(final ForwardingReceiver response, final int id) throws IOException {
			this.response = response;
			log = FileChannel.open(logPath.resolve(String.format("output.%04d", id)), APPEND, CREATE_NEW, SYNC);
		}

		@Override
		public void accept(final ByteBuffer buffer) throws IOException {
			if(log.write(buffer.slice()) != buffer.remaining()) {
				//TODO: log incomplete log
			}
			response.write(buffer);
		}

		public void stop() {
			try {
				try {
					super.close();
				} finally {
					log.close();
				}
			} catch(final IOException e) {
				//TODO: log failure
			}
		}

		@Override
		public void close() {
			stop();
			response.stop();
		}
	}

	private final AtomicInteger index = new AtomicInteger();
	private final Path logPath;
	private final InetSocketAddress remote;

	public TeeCP(final int serverPort, final String remoteHost, final int remotePort, final Path logPath) throws IOException {
		super();
		remote = new InetSocketAddress(remoteHost, remotePort);
		this.logPath = logPath;
		bind(new InetSocketAddress(serverPort), supplier(ForwardingReceiver::new));
	}

	public static void main(final String... args) throws IOException {
		final Path tmp = Paths.get("/tmp/teecp");
		Files.createDirectories(tmp);
		final TeeCP tcp = new TeeCP(8080, "echovantage.com", 80, tmp);
		tcp.run();
	}
}
