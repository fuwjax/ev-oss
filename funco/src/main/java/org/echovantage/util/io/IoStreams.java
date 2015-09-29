package org.echovantage.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class IoStreams {
	public static byte[] readAllBytes(final InputStream input) throws IOException {
		final List<ByteBuffer> buffers = new ArrayList<>();
		int c;
		int total = 0;
		byte[] bytes = new byte[4096];
		while((c = input.read(bytes)) != -1) {
			if(c > 0) {
				total += c;
				buffers.add(ByteBuffer.wrap(bytes, 0, c));
				bytes = new byte[4096];
			}
		}
		bytes = new byte[total];
		c = 0;
		for(final ByteBuffer buffer : buffers) {
			c += buffer.remaining();
			buffer.get(bytes, c - buffer.remaining(), buffer.remaining());
		}
		return bytes;
	}
}
