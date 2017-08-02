package org.fuwjax.oss.util.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InputOutputStream {
	private InputStream in = new InputStream() {
		@Override
		public int read() throws IOException {
			lock.lock();
			try {
				while (!closing && readCapacity() == 0) {
					hasCapacity.await();
				}
				int b = forRead().get();
				hasCapacity.signalAll();
				return b;
			} catch (BufferUnderflowException e) {
				return -1;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return -1;
			} finally {
				lock.unlock();
			}
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			lock.lock();
			try {
				while (!closing && readCapacity() == 0) {
					hasCapacity.await();
				}
				int count = Math.min(readCapacity(), len);
				forRead().get(b, off, count);
				hasCapacity.signalAll();
				return count;
			} catch (BufferUnderflowException e) {
				return -1;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return -1;
			} finally {
				lock.unlock();
			}
		}

		@Override
		public int available() throws IOException {
			return readCapacity();
		}

		@Override
		public void close() {
			InputOutputStream.this.close();
		}
	};

	private OutputStream out = new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			lock.lock();
			try {
				while (!closing && writeCapacity() == 0) {
					hasCapacity.await();
				}
				forWrite().put((byte) b);
				hasCapacity.signalAll();
			} catch (BufferOverflowException e) {
				throw new IOException("cannot write after close");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				lock.unlock();
			}
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			int offset = off;
			int length = len;
			lock.lock();
			try {
				while (!closing && length > 0) {
					while (!closing && writeCapacity() == 0) {
						hasCapacity.await();
					}
					int count = Math.min(writeCapacity(), length);
					forWrite().put(b, offset, length);
					hasCapacity.signalAll();
					offset += count;
					length -= count;
				}
			} catch (BufferOverflowException e) {
				throw new IOException("cannot write after close");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				lock.unlock();
			}
		}

		@Override
		public void close() {
			InputOutputStream.this.close();
		}
	};

	public enum Mode {
		READ, WRITE;
	}

	private final Lock lock = new ReentrantLock();
	private final Condition hasCapacity = lock.newCondition();
	private final ByteBuffer buffer = ByteBuffer.allocate(4096);
	private volatile Mode mode = Mode.WRITE;
	private volatile boolean closing;

	private int readCapacity() {
		return mode == Mode.WRITE ? buffer.position() : buffer.remaining();
	}

	private int writeCapacity() {
		return mode == Mode.WRITE ? buffer.remaining() : buffer.capacity() - buffer.remaining();
	}

	private ByteBuffer forRead() {
		if (mode == Mode.WRITE) {
			buffer.flip();
			mode = Mode.READ;
		}
		return buffer;
	}

	private ByteBuffer forWrite() {
		if (mode == Mode.READ) {
			buffer.compact();
			mode = Mode.WRITE;
		}
		return buffer;
	}

	public InputStream input() {
		return in;
	}

	public OutputStream output() {
		return out;
	}

	public void close() {
		closing = true;
	}
}
