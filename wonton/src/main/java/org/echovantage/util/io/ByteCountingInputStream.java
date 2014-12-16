package org.echovantage.util.io;

import java.io.IOException;
import java.io.InputStream;

public class ByteCountingInputStream extends InputStream {
	private final InputStream input;
	private long count;
	private long mark;

	public ByteCountingInputStream(final InputStream input) {
		this.input = input;
	}

	@Override
	public int read() throws IOException {
		int b = input.read();
		if(b != -1) {
			count++;
		}
		return b;
	}

	@Override
	public int available() throws IOException {
		return input.available();
	}

	@Override
	public synchronized void mark(final int readlimit) {
		input.mark(readlimit);
		mark = count;
	}

	@Override
	public synchronized void reset() throws IOException {
		input.reset();
		if(markSupported()) {
			count = mark;
		}
	}

	@Override
	public boolean markSupported() {
		return input.markSupported();
	}

	@Override
	public int read(final byte[] b) throws IOException {
		int c = input.read(b);
		count += c;
		return c;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		int c = input.read(b, off, len);
		count += c;
		return c;
	}

	@Override
	public long skip(final long n) throws IOException {
		long c = input.skip(n);
		count += c;
		return c;
	}

	public long count() {
		return count;
	}

	@Override
	public void close() throws IOException {
		input.close();
	}
}
