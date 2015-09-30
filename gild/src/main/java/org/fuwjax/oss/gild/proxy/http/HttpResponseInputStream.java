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
package org.fuwjax.oss.gild.proxy.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

public class HttpResponseInputStream extends InputStream {
	private final ReadableByteChannel in;
	private final ByteBuffer buffer = ByteBuffer.allocate(1024);
	private final static String CONTENT_LENGTH = "\r\nContent-Length:";
	private static final byte[] CLUP = CONTENT_LENGTH.toUpperCase().getBytes(Charset.forName("UTF-8"));
	private static final byte[] CLLO = CONTENT_LENGTH.toLowerCase().getBytes(Charset.forName("UTF-8"));
	private int clPos;
	private long contentLength;
	private int emptyLine;
	private boolean header = true;
	private long bytesRead;
	private final boolean mayHaveContent;

	public HttpResponseInputStream(final InputStream resp, final InputStream req) throws IOException {
		this(resp, responseHasContent(req));
	}

	public HttpResponseInputStream(final InputStream in, final boolean mayHaveContent) {
		this.mayHaveContent = mayHaveContent;
		this.in = Channels.newChannel(in);
		buffer.flip();
	}

	@Override
	public int read() throws IOException {
		return ensure(1) ? buffer.get() : -1;
	}

	private boolean ensure(final int remaining) throws IOException {
		if(buffer.remaining() >= remaining) {
			return true;
		}
		if(header || bytesRead < contentLength) {
			buffer.compact();
			final int count = in.read(buffer);
			buffer.flip();
			checkContentLength(count);
		}
		return buffer.hasRemaining();
	}

	private void checkContentLength(final int count) {
		if(count > -1) {
			if(!header) {
				bytesRead += count;
			} else {
				int p = buffer.limit() - count;
				for(; p < buffer.limit(); p++) {
					final int ch = buffer.get(p);
					if(ch == '\r') {
						clPos = 0;
					}
					if(ch == '\r' || ch == '\n') {
						emptyLine++;
						if(emptyLine == 4) {
							header = false;
							bytesRead = buffer.limit() - p - 1;
							return;
						}
					} else {
						emptyLine = 0;
					}
					if(clPos >= CLUP.length) {
						if(ch >= '0' && ch < '9' && mayHaveContent) {
							contentLength = contentLength * 10 + (ch - '0');
						}
					} else if(ch == CLUP[clPos] || ch == CLLO[clPos]) {
						clPos++;
					} else {
						clPos = 0;
					}
				}
			}
		}
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		if(!ensure(len)) {
			return -1;
		}
		final int length = buffer.remaining() > len ? len : buffer.remaining();
		buffer.get(b, off, length);
		return length;
	}

	@Override
	public void close() throws IOException {
		in.close();
		super.close();
	}

	@Override
	public int available() throws IOException {
		return buffer.remaining();
	}

	@Override
	public long skip(final long n) throws IOException {
		int remaining = (int) Math.min(1024, n);
		ensure(remaining);
		remaining = Math.min(remaining, buffer.remaining());
		buffer.position(remaining + buffer.position());
		return remaining;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void mark(final int readlimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	public static boolean responseHasContent(final InputStream request) throws IOException {
		final ReadableByteChannel in = Channels.newChannel(request);
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		in.read(buffer);
		return buffer.asCharBuffer().toString().startsWith("HEAD");
	}
}
