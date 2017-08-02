package org.fuwjax.oss.util.pipe;

import static org.fuwjax.oss.util.function.Unsafe.unsafe;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BufferedReaderSource implements PipeSource {
	private AtomicBoolean closed = new AtomicBoolean(true);
	private Thread reader;
	private Supplier<? extends BufferedReader> factory;
	private Consumer<? super BufferedReader> closer;

	public BufferedReaderSource(Supplier<? extends BufferedReader> factory) {
		this(factory, unsafe(BufferedReader::close));
	}

	public BufferedReaderSource(Supplier<? extends BufferedReader> factory, Consumer<? super BufferedReader> closer) {
		this.factory = factory;
		this.closer = closer;
	}

	@Override
	public void readLines(Consumer<String> handler) {
		assert handler != null;
		assert reader == null;
		if (closed.compareAndSet(true, false)) {
			reader = new Thread(unsafe(() -> readLoop(handler)));
			reader.setDaemon(true);
			reader.start();
		}
	}

	@Override
	public void close() {
		try {
			closed.set(true);
			if (reader != null) {
				reader.join();
				reader = null;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void readLoop(Consumer<String> handler) throws IOException {
		BufferedReader input = null;
		try {
			input = factory.get();
			while (!closed.get() && !Thread.currentThread().isInterrupted()) {
				if (input.ready()) {
					String line = input.readLine();
					if (line == null) {
						closed.set(true);
					} else {
						handler.accept(line);
					}
				} else {
					Thread.yield();
				}
			}
		} finally {
			if(input != null) {
				closer.accept(input);
			}
		}
	}
}
