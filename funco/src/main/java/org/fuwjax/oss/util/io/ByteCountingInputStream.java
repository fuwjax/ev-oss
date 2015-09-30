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
package org.fuwjax.oss.util.io;

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
